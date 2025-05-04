package pt.up.fe.comp2025.optimization.refalloc;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.Instruction;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class RegGraph {
    private final List<Report> reports = new ArrayList<>();
    private final Map<String, Set<String>> interferenceGraph = new HashMap<>();
    private final Map<String, Integer> colorMapping = new HashMap<>();
    private final Stack<String> stack = new Stack<>();

    private final Set<String> variables = new HashSet<>();

    private final Method method;
    private int minRegs;
    private int numRegs;
    private int start_color = 0;


    public RegGraph(Method method) {
        this.method = method;
    }


    private int calculateMinimumRegistersNeeded() {
        int maxDegree = 0;
        for (Set<String> neighbors : interferenceGraph.values()) {
            maxDegree = Math.max(maxDegree, neighbors.size());
        }

        int reserved = method.isStaticMethod() ? 0 : 1;
        reserved += method.getParams().size();

        this.start_color = reserved;

        return Math.max(minRegs, maxDegree + 1);
    }


    private void initGraph() {
        interferenceGraph.clear();

        // Adicionar nós para cada variável local (excluindo this e parâmetros)
        for (Map.Entry<String, Descriptor> entry : method.getVarTable().entrySet()) {
            String varName = entry.getKey();
            Descriptor descriptor = entry.getValue();

            if (isEligibleForAllocation(varName, descriptor)) {
                interferenceGraph.put(varName, new HashSet<>());
                variables.add(varName);
            }
        }

        this.minRegs = calculateMinimumRegistersNeeded();
    }


    private boolean isEligibleForAllocation(String varName, Descriptor descriptor) {
        // Excluir this e variáveis de campo
        if (varName.equals("this") || descriptor.getScope() == VarScope.FIELD ||
                descriptor.getScope() == VarScope.PARAMETER) {
            return false;
        }
        return true;
    }


    public void buildInterferenceGraph(Map<Instruction, Set<String>> livenessInfo) {
        initGraph();

        // Para cada instrução, as variáveis vivas ao mesmo tempo interferem umas com as outras
        for (Set<String> liveVars : livenessInfo.values()) {
            List<String> allocatableVars = new ArrayList<>();

            // Filtrar apenas variáveis elegíveis para alocação
            for (String var : liveVars) {
                Descriptor descriptor = method.getVarTable().get(var);
                if (descriptor != null && isEligibleForAllocation(var, descriptor)) {
                    allocatableVars.add(var);
                }
            }

            // Adicionar arestas entre todas as variáveis vivas ao mesmo tempo
            for (int i = 0; i < allocatableVars.size(); i++) {
                String var1 = allocatableVars.get(i);

                for (int j = i + 1; j < allocatableVars.size(); j++) {
                    String var2 = allocatableVars.get(j);
                    interferenceGraph.get(var1).add(var2);
                    interferenceGraph.get(var2).add(var1);
                }
            }
        }
    }


    public Map<String, Descriptor> colorGraph(int requestedRegs) {
        // Verificar se o número de registradores é válido

        if (!validateRegisterCount(requestedRegs)) {
            return null;
        }

        // Limpar mapeamento de cores anterior
        colorMapping.clear();
        stack.clear();

        // Construir a pilha de variáveis removendo-as do grafo em ordem de menor grau
        buildStack();

        // Colorir o grafo
        boolean success = assignColors();

        // Se não conseguir colorir com o número atual de registradores, tentar com mais
        if (!success) {
            numRegs++;
            return colorGraph(numRegs);
        }

        // Atualizar a tabela de variáveis com os registradores atribuídos
        return updateVarTable();
    }


    private boolean validateRegisterCount(int requestedRegs) {
        if (requestedRegs == 0) {
            // Usar o número mínimo de registradores
            numRegs = minRegs;
            return true;
        }

        if (requestedRegs < minRegs) {
            String message = "Impossible to allocate " + requestedRegs +
                    " registers, minimum is " + minRegs;
            reports.add(Report.newError(Stage.OPTIMIZATION, 0, 0, message, null));
            return false;
        }

        numRegs = requestedRegs;
        return true;
    }


    private void buildStack() {
        Map<String, Set<String>> workGraph = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : interferenceGraph.entrySet()) {
            workGraph.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        // Continuar enquanto o grafo não estiver vazio
        while (!workGraph.isEmpty()) {
            boolean removed = false;

            // Encontrar variável com menos que k arestas
            for (Iterator<Map.Entry<String, Set<String>>> it = workGraph.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Set<String>> entry = it.next();
                String var = entry.getKey();
                Set<String> neighbors = entry.getValue();

                if (neighbors.size() < numRegs) {
                    // Guardar na pilha
                    stack.push(var);

                    // Remover do grafo de trabalho
                    it.remove();

                    // Remover das listas de adjacência
                    for (Set<String> adjList : workGraph.values()) {
                        adjList.remove(var);
                    }

                    removed = true;
                    break;
                }
            }

            // Se não conseguiu remover nenhuma variável, é um spill
            if (!removed && !workGraph.isEmpty()) {
                // Opção 1: Spill (escolher uma variável para remover)
                String spillVar = selectSpillCandidate(workGraph);
                stack.push(spillVar);

                // Remover do grafo de trabalho
                workGraph.remove(spillVar);

                // Remover das listas de adjacência
                for (Set<String> adjList : workGraph.values()) {
                    adjList.remove(spillVar);
                }
            }
        }
    }


    private String selectSpillCandidate(Map<String, Set<String>> workGraph) {
        // Estratégia simples: escolher a variável com mais interferências
        String spillVar = null;
        int maxDegree = -1;

        for (Map.Entry<String, Set<String>> entry : workGraph.entrySet()) {
            if (entry.getValue().size() > maxDegree) {
                maxDegree = entry.getValue().size();
                spillVar = entry.getKey();
            }
        }

        return spillVar;
    }

    private boolean assignColors() {
        // Registradores disponíveis (cores)
        List<Integer> availableColors = new ArrayList<>();
        for (int i = start_color; i < numRegs; i++) {
            availableColors.add(i);
        }

        // Processar variáveis na ordem da pilha (reversa da remoção)
        while (!stack.isEmpty()) {
            String var = stack.pop();
            Set<String> neighbors = interferenceGraph.get(var);

            // Marcar cores já usadas pelos vizinhos
            boolean[] usedColors = new boolean[numRegs];

            for (String neighbor : neighbors) {
                Integer neighborColor = colorMapping.get(neighbor);
                if (neighborColor != null && neighborColor >= start_color) {
                    usedColors[neighborColor] = true;
                }
            }

            // Encontrar a menor cor disponível
            int selectedColor = -1;
            for (int i = start_color; i < numRegs; i++) {
                if (!usedColors[i]) {
                    selectedColor = i;
                    break;
                }
            }

            // Se não encontrou cor disponível, falhar
            if (selectedColor == -1) {
                return false;
            }

            // Atribuir a cor
            colorMapping.put(var, selectedColor);
        }

        return true;
    }


    private Map<String, Descriptor> updateVarTable() {
        Map<String, Descriptor> updatedTable = new HashMap<>();

        for (Map.Entry<String, Descriptor> entry : method.getVarTable().entrySet()) {
            String varName = entry.getKey();
            Descriptor descriptor = entry.getValue();

            // Se a variável foi colorida, atualizar o registrador
            if (colorMapping.containsKey(varName)) {
                int reg = colorMapping.get(varName);
                updatedTable.put(varName, new Descriptor(descriptor.getScope(), reg, descriptor.getVarType()));
            } else {
                // Manter o descritor original
                updatedTable.put(varName, descriptor);
            }
        }

        return updatedTable;
    }


    public Map<String, Set<String>> getInterferenceGraph() {
        return interferenceGraph;
    }


    public Map<String, Integer> getColorMapping() {
        return colorMapping;
    }


    public List<Report> getReports() {
        return reports;
    }
}