package com.mygame.game;

// Both sides' node chains; correct answers hit the enemy, wrong ones hit you.
public final class ParadigmSpace {
    public enum Tier { EARLY, MIDDLE, CORE }

    public static final class Node {
        public final String name;
        public final int maxHp;
        public final Tier tier;
        private int hp;

        Node(String name, int maxHp, Tier tier) {
            this.name = name;
            this.maxHp = maxHp;
            this.tier = tier;
            this.hp = maxHp;
        }

        public int hp() {
            return hp;
        }

        public boolean destroyed() {
            return hp <= 0;
        }
    }

    public static final class Side {
        private final Node[] nodes;

        Side(Node[] nodes) {
            this.nodes = nodes;
        }

        public Node[] nodes() {
            return nodes;
        }

        // nodes.length when the Core is gone
        public int foremostIndex() {
            for (int index = 0; index < nodes.length; index++) {
                if (!nodes[index].destroyed()) {
                    return index;
                }
            }
            return nodes.length;
        }

        public Node foremost() {
            int index = foremostIndex();
            return index < nodes.length ? nodes[index] : null;
        }

        // returns true if a node was destroyed
        public boolean damage(int amount) {
            boolean destroyedNode = false;
            for (int hit = 0; hit < amount; hit++) {
                Node node = foremost();
                if (node == null) {
                    break;
                }
                node.hp--;
                destroyedNode |= node.destroyed();
            }
            return destroyedNode;
        }

        public boolean defeated() {
            return foremost() == null;
        }

        public int totalHp() {
            int total = 0;
            for (Node node : nodes) {
                total += Math.max(0, node.hp);
            }
            return total;
        }

        public int maxTotalHp() {
            int total = 0;
            for (Node node : nodes) {
                total += node.maxHp;
            }
            return total;
        }
    }

    private static final String[] NAMES = {"Procedural Gate", "Object Model", "Functional Register I",
        "Functional Register II", "Logic Shrine I", "Logic Shrine II", "Core: Syntax", "Core: Semantics"};
    private static final Tier[] TIERS = {Tier.EARLY, Tier.EARLY, Tier.MIDDLE, Tier.MIDDLE, Tier.MIDDLE,
        Tier.MIDDLE, Tier.CORE, Tier.CORE};
    // your HP: 48 total (mistakes you can afford)
    private static final int[] ALLY_HP = {4, 4, 6, 6, 6, 6, 8, 8};
    // enemy HP split per node (scaled to the question count)
    private static final int[] ENEMY_WEIGHTS = {15, 20, 18, 18, 18, 18, 19, 19};

    private final Side ally;
    private final Side enemy;

    // Enemy total HP = question count, so winning needs every question answered correctly.
    public ParadigmSpace(int enemyTotalHp) {
        Node[] allyNodes = new Node[NAMES.length];
        for (int index = 0; index < NAMES.length; index++) {
            allyNodes[index] = new Node(NAMES[index], ALLY_HP[index], TIERS[index]);
        }
        ally = new Side(allyNodes);
        enemy = new Side(scaledEnemyNodes(Math.max(NAMES.length, enemyTotalHp)));
    }

    // rounds cumulatively so the node HPs add up exactly to the total
    private static Node[] scaledEnemyNodes(int total) {
        int weightSum = 0;
        for (int weight : ENEMY_WEIGHTS) {
            weightSum += weight;
        }
        Node[] nodes = new Node[NAMES.length];
        int given = 0;
        int weightSoFar = 0;
        for (int index = 0; index < NAMES.length; index++) {
            weightSoFar += ENEMY_WEIGHTS[index];
            int target = (int) Math.round(total * weightSoFar / (double) weightSum);
            int hp = Math.max(1, target - given);
            given += hp;
            nodes[index] = new Node(NAMES[index], hp, TIERS[index]);
        }
        return nodes;
    }

    public Side ally() {
        return ally;
    }

    public Side enemy() {
        return enemy;
    }

    public Tier currentTier() {
        Node node = enemy.foremost();
        return node == null ? Tier.CORE : node.tier;
    }
}
