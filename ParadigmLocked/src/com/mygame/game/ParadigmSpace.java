package com.mygame.game;

/*
    Paradigm Space: one execution lane, each side guarded by the same chain of Paradigm Nodes.
    Nodes must be destroyed in order (foremost first):
        Procedural Gate -> Object Model -> Functional Register x2 -> Logic Shrine x2 -> Core (2 phases)
    A correct answer damages the ENEMY's foremost node, a wrong answer damages YOUR foremost node.
    Destroying the enemy Core (both phases) wins; losing your own Core loses.
 */
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

    /** One side (yours or the enemy's). */
    public static final class Side {
        private final Node[] nodes = createNodes();

        public Node[] nodes() {
            return nodes;
        }

        /** Index of the foremost standing node, or nodes.length when the Core is gone. */
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

        /** Deals damage to the foremost node; returns true if that hit destroyed a node. */
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

    private final Side ally = new Side();
    private final Side enemy = new Side();

    private static Node[] createNodes() {
        return new Node[] {
            new Node("Procedural Gate", 2, Tier.EARLY),
            new Node("Object Model", 2, Tier.EARLY),
            new Node("Functional Register I", 3, Tier.MIDDLE),
            new Node("Functional Register II", 3, Tier.MIDDLE),
            new Node("Logic Shrine I", 3, Tier.MIDDLE),
            new Node("Logic Shrine II", 3, Tier.MIDDLE),
            new Node("Core: Syntax", 4, Tier.CORE),
            new Node("Core: Semantics", 4, Tier.CORE)
        };
    }

    public Side ally() {
        return ally;
    }

    public Side enemy() {
        return enemy;
    }

    /** Difficulty follows how deep you are into the enemy lane. */
    public Tier currentTier() {
        Node node = enemy.foremost();
        return node == null ? Tier.CORE : node.tier;
    }
}
