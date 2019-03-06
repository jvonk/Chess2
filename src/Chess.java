import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Chess {
    public Chess() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            JFrame frame = new JFrame("Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ChessBoardPane g = new ChessBoardPane(new int[][]{{-4,-3,-2,-5,-6,-2,-3,-4},{-1,-1,-1,-1,-1,-1,-1,-1},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0},{1,1,1,1,1,1,1,1},{4,3,2,5,6,2,3,4}});
            JToolBar tools = new JToolBar();
            tools.setFloatable(false);
            tools.setOpaque(true);
            tools.add(new AbstractAction("Resign") {
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane.showConfirmDialog(null, "ARE YOU SURE?", "RESIGN", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        g.set(true);
                    }
                }
            });
            tools.addSeparator();
            tools.add(new AbstractAction("Save") {
                public void actionPerformed(ActionEvent e) {
                    String name = JOptionPane.showInputDialog("What do you want to call the save file? ");
                    try {
                        Files.write(Paths.get("save.txt"), ("@"+name+"\n"+g.toString()+"\n"+Files.readString(Paths.get("save.txt"))).getBytes());
                    } catch (Exception ex) {
                        System.exit(1);
                    }
                }
            });
            tools.add(new AbstractAction("Load") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        String s = Files.readString(Paths.get("save.txt"));
                        String[] strings = s.split("\n@");
                        Map<String, String> find = new HashMap<>();
                        for (int i = 0; i < strings.length; i++) {
                            int index = strings[i].indexOf('\n');
                            find.put(strings[i].substring(i==0?1:0, index), strings[i].substring(index+1));
                        }
                        String[] names = find.keySet().toArray(new String[0]);
                        g.set(find.get(JOptionPane.showInputDialog(null, "Which save? ", "Save", JOptionPane.QUESTION_MESSAGE, null, names, names[0]).toString()));
                    } catch (Exception ex) {
                        System.exit(1);
                    }
                }
            });
            tools.addSeparator();
            tools.add(new AbstractAction("Undo") {
                public void actionPerformed(ActionEvent e) {
                    g.undo();
                    if (g.ai!=0) g.undo();
                }
            });
            tools.addSeparator();
            tools.add(new JLabel("Difficulty: "));
            JComboBox<String> ai = new JComboBox<>(new String[] {"None", "Very Easy", "Easy", "Medium", "Hard", "Very Hard"});
            ai.addActionListener ((ActionEvent e) -> {
                g.ai=((JComboBox)e.getSource()).getSelectedIndex();
            });
            ai.setMaximumSize(ai.getPreferredSize());
            tools.add(ai);
            frame.add(tools, BorderLayout.PAGE_START);
            frame.add(g);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new Chess();
    }

    class ChessBoardPane extends JPanel {
        private Cell[][] board;
        private final Image[][] images;
        private Map<Point, Set<Point>> moves;
        private boolean turn;
        private Set<Point> ends;
        private final int[][] chessBoard;
        private java.util.List<int[][]> prevMoves;
        private Point dragStart;
        public int ai;
        public int[][][] posValues = new int[][][]{new int[][]{new int[]{0,0,0,0,0,0,0,0},new int[]{10,10,10,10,10,10,10,10},new int[]{2,2,4,6,6,4,2,2},new int[]{1,1,2,5,5,2,1,1},new int[]{0,0,0,4,4,0,0,0},new int[]{1,-1,-2,0,0,-2,-1,1},new int[]{1,2,2,-4,-4,2,2,1},new int[]{0,0,0,0,0,0,0,0}},new int[][]{new int[]{-4,-2,-2,-2,-2,-2,-2,-4},new int[]{-2,0,0,0,0,0,0,-2},new int[]{-2,0,1,2,2,1,0,-2},new int[]{-2,1,1,2,2,1,1,-2},new int[]{-2,0,2,2,2,2,0,-2},new int[]{-2,2,2,2,2,2,2,-2},new int[]{-2,1,0,0,0,0,1,-2},new int[]{-4,-2,-2,-2,-2,-2,-2,-4}},new int[][]{new int[]{-10,-8,-6,-6,-6,-6,-8,-10},new int[]{-8,-4,0,0,0,0,-4,-8},new int[]{-6,0,2,3,3,2,0,-6},new int[]{-6,1,3,4,4,3,1,-6},new int[]{-6,0,3,4,4,3,0,-6},new int[]{-6,1,2,3,3,2,1,-6},new int[]{-8,-4,0,1,1,0,-4,-8},new int[]{-10,-8,-6,-6,-6,-6,-8,-10}},new int[][]{new int[]{0,0,0,0,0,0,0,0},new int[]{1,2,2,2,2,2,2,1},new int[]{-1,0,0,0,0,0,0,-1},new int[]{-1,0,0,0,0,0,0,-1},new int[]{-1,0,0,0,0,0,0,-1},new int[]{-1,0,0,0,0,0,0,-1},new int[]{-1,0,0,0,0,0,0,-1},new int[]{0,0,0,1,1,0,0,0}},new int[][]{new int[]{-4,-2,-2,-1,-1,-2,-2,-4},new int[]{-2,0,0,0,0,0,0,-2},new int[]{-2,0,1,1,1,1,0,-2},new int[]{-1,0,1,1,1,1,0,-1},new int[]{0,0,1,1,1,1,0,-1},new int[]{-2,1,1,1,1,1,0,-2},new int[]{-2,0,1,0,0,0,0,-2},new int[]{-4,-2,-2,-1,-1,-2,-2,-4}},new int[][]{new int[]{-6,-8,-8,-10,-10,-8,-8,-6},new int[]{-6,-8,-8,-10,-10,-8,-8,-6},new int[]{-6,-8,-8,-10,-10,-8,-8,-6},new int[]{-6,-8,-8,-10,-10,-8,-8,-6},new int[]{-4,-6,-6,-8,-8,-6,-6,-4},new int[]{-2,-4,-4,-4,-4,-4,-4,-2},new int[]{4,4,0,0,0,0,4,4},new int[]{4,6,2,0,0,2,6,4}}};
        public int[] typeValues = new int[] {0, 1, 3, 3, 5, 9, 200};
        ChessBoardPane(int[][] board) {
            ai=0;
            images = new Image[2][6];
            chessBoard=board;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 6; j++) {
                    try {
                        images[i][j] = ImageIO.read(new File("image/" + (i == 0 ? "BLACK" : "WHITE") + "_" + new String[]{"PAWN", "BISHOP", "KNIGHT", "ROOK", "QUEEN", "KING"}[j] + ".png"));
                    } catch (Exception ec) {
                        System.exit(1);
                    }
                }
            }
            set(true);
        }

        int calcValue() {
            int total = 0;
            int[][][] sums = new int[2][2][board.length];
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    int t = board[i][j].type;
                    int sgn = t<0?0:1;
                    sums[sgn][0][i]++;
                    sums[sgn][1][j]++;
                    if (i+sgn>=0 && i+sgn<board.length && board[i+sgn][j].type!=0) total-=2*sgn;
                    if (t==0) continue;
                    total += Integer.signum(t) * (typeValues[Math.abs(t)]*20 + posValues[Math.abs(t)-1][j][(t>0||Math.abs(t)==3||Math.abs(t)==5)?i:board.length-i-1]);
                }
            }
            for (int i = 0; i < board.length; i++) {
                total+=2*(sums[0][0][i]>1?sums[0][0][i]:0+sums[0][1][i]>1?sums[0][1][i]:0)-2*(sums[1][0][i]>1?sums[1][0][i]:0+sums[1][1][i]>1?sums[1][1][i]:0);
            }
            return total;
        }

        public void setArray(int[][] before) {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    board[i][j].setInt(before[i][j]);
                }
            }
        }

        public void resetIcons() {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    board[i][j].resetIcon(images);
                }
            }
        }

        public void move() {
            boolean startTurn=turn;
            calcMoves(turn);
            int[][] before = toMatrix();
            Point start = null, end = null;
            int max = startTurn?Integer.MIN_VALUE:Integer.MAX_VALUE;
            HashMap<Point, Set<Point>> old = new HashMap<>(moves);
            for (Point s : old.keySet()) {
                for (Point e : old.get(s)) {
                    move(s, e);
                    int val = minimax(ai-1, Integer.MIN_VALUE, Integer.MAX_VALUE, !startTurn);
                    setArray(before);
                    if (startTurn?val>max:val<max) {
                        max=val;
                        start=s;
                        end=e;
                    }
                }
            }
            prevMoves.add(before);
            move(start, end);
            turn=!startTurn;
        }

        int minimax(int depth, int alpha, int beta, boolean startTurn) {
            if (depth==0) return -calcValue();
            calcMoves(turn);
            int[][] before = toMatrix();
            int max = startTurn?Integer.MAX_VALUE:Integer.MIN_VALUE;
            HashMap<Point, Set<Point>> old = new HashMap<>(moves);
            for (Point s : old.keySet()) {
                for (Point e : old.get(s)) {
                    basicMove(s, e);
                    int res = minimax(depth-1, alpha, beta, !startTurn);
                    if (startTurn?res<max:res>max) {
                        calcMoves(turn);
                        if (!ends.contains(getPiece(turn?6:-6))) {
                            max = res;
                        }
                        if (startTurn) {
                            beta=Math.min(beta, max);
                        } else {
                            alpha=Math.max(alpha, max);
                        }
                        if (beta<=alpha) {
                            return max;
                        }
                    }
                    turn=!turn;
                    setArray(before);
                }
            }
            return max;
        }

        void set(boolean original) {
            dragStart=null;
            turn = false;
            moves = new HashMap<>();
            prevMoves = new ArrayList<>();
            removeAll();
            setLayout(new ChessBoardLayoutManager());
            if (original) board = new Cell[8][8];
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (original) {
                        board[row][col] = new Cell((row + col) % 2 == 0);
                        final Point p = new Point(row, col);
                        board[row][col].addActionListener((ActionEvent e) -> {
                            if (dragStart == null) {
                                if (getCell(p).type<0==turn) {
                                    dragStart = p;
                                    getCell(p).setSelected(true);
                                    update();
                                }
                                return;
                            }
                            if (moves.containsKey(dragStart) && moves.get(dragStart).contains(p)) {
                                prevMoves.add(toMatrix());
                                move(dragStart, p);
                                if (ai>0) {
                                    resetIcons();
                                    move();
                                }
                            }
                            getCell(dragStart).setSelected(false);
                            dragStart = null;
                            update();
                        });
                        board[row][col].addComponentListener(new ComponentAdapter() {
                            @Override
                            public void componentResized(ComponentEvent e) {
                            Cell btn = (Cell) e.getComponent();
                            btn.resetIcon(images);
                            }
                        });
                    }
                    board[row][col].moved = false;
                    board[row][col].type = chessBoard[row][col];
                    add(board[row][col], new Point(col, row));
                    board[row][col].setSize(1, 1); //to update cells
                }
            }
            update();
        }

        void set(String s) {
            String[] a = s.split("\n");
            turn = Boolean.parseBoolean(a[0]);
            moves = new HashMap<>();
            setLayout(new ChessBoardLayoutManager());
            for (int row = 0; row < 8; row++) {
                String[] b = a[row+1].substring(1, a[row+1].length()-1).split(", ");
                for (int col = 0; col < 8; col++) {
                    int c = Integer.parseInt(b[col]);
                    board[row][col].setInt(c);
                    board[row][col].resetIcon(images);
                }
            }
            update();
        }

        void undo() {
            boolean startTurn = turn;
            int size = prevMoves.size();
            if (size<1) return;
            setArray(prevMoves.remove(size-1));
            turn = !startTurn;
            update();
        }

        void calcMoves(boolean t) {
            boolean oldTurn = turn;
            turn = t;
            moves.clear();
            Point[] d = new Point[]{new Point(-1, -1), new Point(-1, 0), new Point(-1, 1), new Point(0, -1), new Point(0, 1), new Point(1, -1), new Point(1, 0), new Point(1, 1)};
            Point[] knight = new Point[]{new Point(-2, -1), new Point(-2, 1), new Point(-1, -2), new Point(-1, 2), new Point(1, -2), new Point(1, 2), new Point(2, -1), new Point(2, 1)};
            for (int i = 0; i < board.length; i++) {
                inner:
                for (int j = 0; j < board[i].length; j++) {
                    Cell c = board[i][j];
                    Point start = new Point(i, j);
                    switch (Math.abs(c.type)) {
                        case 0:
                            continue inner;
                        case 1: {
                            int dir = -Integer.signum(c.type);
                            Cell up = getCell(new Point(start.x + dir, start.y));
                            Cell[] upMove = new Cell[] {getCell(new Point(start.x + dir, start.y-1)), getCell(new Point(start.x + dir, start.y+1))};
                            if (up!=null&&up.type == 0) {
                                add(start, new Point(dir, 0));
                            }
                            for (int k = 0; k < upMove.length; k++) {
                                if (upMove[k]!=null&&upMove[k].type != 0) {
                                    add(start, new Point(dir, k*2-1));
                                }
                            }
                            Cell upTwo = getCell(new Point(start.x + dir * 2, start.y));
                            if (upTwo!=null&&!c.moved && upTwo.type == 0&&getCell(new Point(start.x + dir, start.y)).type==0) {
                                add(start, new Point(dir * 2, 0));
                            }
                            if (i==3&&dir==-1||i==4&&dir==1) {
                                Cell[] enPassantTo = new Cell[] {getCell(new Point(start.x + dir, start.y-1)), getCell(new Point(start.x + dir, start.y+1))};
                                Cell[] enPassantCapture = new Cell[] {getCell(new Point(start.x, start.y-1)), getCell(new Point(start.x, start.y+1))};
                                for (int k = 0; k < 2; k++) {
                                    if (enPassantCapture[k]!=null&&enPassantCapture[k].type == dir&&enPassantTo[k]!=null&&enPassantTo[k].type==0
                                            &&prevMoves.get(prevMoves.size()-1)[start.x][start.y+k*2-1]/2==0) {
                                        add(start, new Point(dir, k*2-1));
                                    }
                                }
                            }
                        }
                        break;
                        case 2:
                            checkLines(new int[]{0, 2, 5, 7}, d, start);
                            break;
                        case 3: {
                            for (Point end : knight) add(start, end);
                        }
                        break;
                        case 4:
                            checkLines(new int[]{1, 3, 4, 6}, d, start);
                            break;
                        case 5:
                            checkLines(new int[]{0, 1, 2, 3, 4, 5, 6, 7}, d, start);
                            break;
                        case 6: {
                            for (Point end : d) {
                                add(start, end);
                            }
                            if (!c.moved && ends!=null && !ends.contains(start)) {
                                loop: for (int k = 0; k < 2; k++) {
                                    Cell end = getCell(new Point(start.x, k==0?0:board.length-1));
                                    if (end!=null&&end.type==4&&!end.moved) {
                                        for (int l = start.y+k*2-1; Math.abs(l-start.y)<=2; l+=k*2-1) {
                                            if (board[start.x][l].type!=0 || ends.contains(new Point(start.x, l))) continue loop;
                                        }
                                        add(start, new Point(0, k*4-2));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ends = new HashSet<>();
            for (Set<Point> set : moves.values()) {
                ends.addAll(set);
            }
            turn=oldTurn;
        }

        void add(Point s, Point change) {
            Cell start = board[s.x][s.y];
            if (start.type == 0 || start.type < 0 != turn) return;
            Point e = new Point(s.x + change.x, s.y + change.y);
            if (!checkBounds(e)) return;
            Cell end = board[e.x][e.y];
            if (1.0 * end.type / start.type > 0) return;
            moves.putIfAbsent(s, new HashSet<>());
            moves.get(s).add(e);
        }

        void checkLines(int[] which, Point[] d, Point start) {
            for (int l : which) {
                for (int k = 1; k < 16; k++) {
                    Point end = new Point(k * d[l].x, k * d[l].y);
                    if (start.x + end.x < 0 || start.x + end.x >= board.length || start.y + end.y < 0 || start.y + end.y >= board[start.x + end.x].length) {
                        break;
                    }
                    add(start, end);
                    if (board[start.x + end.x][start.y + end.y].type != 0) {
                        break;
                    }
                }
            }
        }

        void basicMove(Point f, Point t) {
            Cell from = getCell(f);
            Cell to = getCell(t);
            boolean promotion = Math.abs(from.type)==1&&(t.x==0||t.x==board.length-1);
            if (to.type==0&&Math.abs(from.type)==1&&Math.abs(t.y-f.y)==1) {
                board[f.x][t.y].type=0;
                board[f.x][t.y].moved=false;
                board[f.x][t.y].resetIcon(images);
            }
            if (Math.abs(from.type)==6&&Math.abs(t.y-f.y)>1) {
                Cell castle = getCell(new Point(f.x, t.y-f.y<0?0:board.length-1));
                Cell blank = getCell(new Point(f.x, t.y-f.y<0?3:board.length-3));
                movePiece(castle, blank);
                turn=!turn;
            }
            movePiece(from, to);

            if (promotion) {
                int sgn = Integer.signum(to.type);
                int i = 0;
                int[] arr = new int[] {5, 4, 3, 2, 1};
                do {
                    if (i >= arr.length) {
                        undoMovePiece(from, to);
                        to.setSelected(false);
                        break;
                    }
                    to.type= sgn*arr[i++];
                    calcMoves(turn);
                } while (calcMate());
            }
        }

        void move(Point f, Point t) {
            Cell from = getCell(f);
            Cell to = getCell(t);
            basicMove(f, t);
            Point king = getPiece(turn ? 6 : -6);
            calcMoves(turn);
            boolean inCheck = ends.contains(king);
            calcMoves(!turn);

            if (inCheck) {
                undoMovePiece(from, to);
                to.setSelected(false);
            }
            if (inCheck||calcMate()) {
                return;
            }

            calcMoves(turn);

            from.setSelected(false);
            to.setSelected(false);

            king = getPiece(turn ? -6 : 6);
            if (!calcMate()) {
                return;
            }
            HashMap<Point, Set<Point>> old = new HashMap<>(moves);
            Set<Point> oldEnds = new HashSet<>(ends);
            calcMoves(!turn);
            boolean check = ends.contains(king);
            if (JOptionPane.showConfirmDialog(null, (check ? (turn ? "White" : "Black") + " wins! " : "") + "Restart?", check ? "Checkmate" : "Stalemate", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                this.removeAll();
                set(false);
                update();
            } else {
                moves=old;
                ends=oldEnds;
            }
        }

        boolean calcMate() {
            Point king = getPiece(turn ? -6 : 6);
            Map<Point, Set<Point>> old = new HashMap<>(moves);
            if (old.containsKey(king)) {
                Set<Point> kingSet = old.get(king);
                Cell s = getCell(king);
                for (Point end : kingSet) {
                    Cell e = getCell(end);
                    movePiece(s, e);
                    calcMoves(turn);
                    undoMovePiece(s, e);
                    moves = new HashMap<>(old);
                    if (!ends.contains(end)) {
                        return false;
                    }
                }
            }
            int[][] before = toMatrix();
            for (Map.Entry<Point, Set<Point>> set : old.entrySet()) {
                Point start = set.getKey();
                if (start.equals(king)) continue;
                if (board[start.x][start.y].type < 0 != turn) continue;
                for (Point end : set.getValue()) {
                    basicMove(start, end);
                    calcMoves(turn);
                    turn=!turn;
                    setArray(before);
                    moves = new HashMap<>(old);
                    if (!ends.contains(getPiece(turn ? -6 : 6))) {
                        return false;
                    }
                }
            }
            return true;
        }

        void movePiece(Cell from, Cell to) {
            if (to == null || from == null || from == to) return;
            from.save();
            to.save();
            to.type = from.type;
            from.type = 0;
            to.moved = true;
            from.moved = false;
            calcMoves(turn);
            turn = !turn;
        }

        void undoMovePiece(Cell from, Cell to) {
            if (to == null || from == null || from == to) return;
            from.undo();
            to.undo();
            turn = !turn;
        }

        void update() {
            resetIcons();
            calcMoves(!turn);
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    Color color = board[i][j].black ? Color.DARK_GRAY : Color.LIGHT_GRAY;
                    double fraction = 0;
                    if (board[i][j].isSelected()) {
                        fraction += 0.5;
                    }
                    boolean check = ends.contains(new Point(i, j)) && board[i][j].type != 0;
                    if (fraction != 0 || check) {
                        int red = (int) Math.min(255, color.getRed() * (check?2:1) * (1 - fraction) + 255 * fraction);
                        int green = (int) Math.min(255, color.getGreen() * (1 - fraction) + 255 * fraction);
                        int blue = (int) Math.min(255, color.getBlue() * (1 - fraction) + 255 * fraction);
                        color = new Color(red, green, blue);
                    }
                    board[i][j].setBackground(color);
                }
            }
            calcMoves(turn);
        }

        Point getPiece(int type) {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j].type == type) return new Point(i, j);
                }
            }
            return null;
        }

        boolean checkBounds(Point p) {
            return (p.x>=0&&p.x<board.length&&p.y>=0&&p.y<board[p.x].length);
        }

        Cell getCell(Point p) {
            return checkBounds(p)?board[p.x][p.y]:null;
        }

        int[][] toMatrix() {
            int[][] values = new int[board.length][board[0].length];
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    values[i][j]=board[i][j].toInt();
                }
            }
            return values;
        }

        public String toString() {
            StringBuilder b = new StringBuilder();
            int[][] current = toMatrix();
            b.append(turn);
            for (int i = 0; i < board.length; i++) {
                b.append("\n");
                b.append(Arrays.toString(current[i]));
            }
            return b.toString();
        }
    }

    class Cell extends JButton {
        final boolean black;
        boolean moved;
        boolean oldMoved;
        int type;
        int oldType;

        Cell(boolean black) {
            this.black = black;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(true);
        }

        void save() {
            oldType = type;
            oldMoved = moved;
        }

        void undo() {
            type = oldType;
            moved = oldMoved;
        }

        public String toString() {
            return ("@"+this.type+";"+(moved?1:0));
        }

        int toInt() {
            return type*2+(moved?Integer.signum(type):0);
        }

        void setInt(int in) {
            type=in/2;
            moved=in%2!=0;
        }

        void resetIcon(Image[][] images) {
            this.setIcon(this.type == 0 ? null : new ImageIcon(images[type < 0 ? 0 : 1][Math.abs(type) - 1].getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST)));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(25, 25);
        }
    }

    class ChessBoardLayoutManager implements LayoutManager2 {
        private final Map<Point, Component> mapComps = new HashMap<>(25);

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints instanceof Point) {
                mapComps.put((Point) constraints, comp);
            } else throw new IllegalArgumentException("ChessBoard constraints must be a Point");
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            return preferredLayoutSize(target);
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0.5f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0.5f;
        }

        public void invalidateLayout(Container target) {
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            Point[] keys = mapComps.keySet().toArray(new Point[0]);
            for (Point p : keys) {
                if (mapComps.get(p).equals(comp)) {
                    mapComps.remove(p);
                    break;
                }
            }
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new CellGrid(mapComps).getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            CellGrid grid = new CellGrid(mapComps);
            int cellSize = Math.min(parent.getWidth(), parent.getHeight()) / Math.max(Math.max(grid.mapRows.size(), grid.mapCols.size()), 1);
            int xOffset = (parent.getWidth() - (cellSize * grid.mapCols.size())) / 2;
            int yOffset = (parent.getHeight() - (cellSize * grid.mapRows.size())) / 2;
            for (Integer row : grid.mapRows.keySet()) {
                for (CellGrid.Cell cell : grid.mapRows.get(row)) {
                    Component comp = cell.component;
                    comp.setLocation(xOffset + (cell.point.x * cellSize), yOffset + (cell.point.y * cellSize));
                    comp.setSize(cellSize, cellSize);
                }
            }
        }

        class CellGrid {
            final Map<Integer, java.util.List<Cell>> mapRows;
            final Map<Integer, java.util.List<Cell>> mapCols;
            private final Dimension prefSize;

            CellGrid(Map<Point, Component> mapComps) {
                mapRows = new HashMap<>(25);
                mapCols = new HashMap<>(25);
                for (Point p : mapComps.keySet()) {
                    mapRows.putIfAbsent(p.y, new ArrayList<>(25));
                    mapCols.putIfAbsent(p.x, new ArrayList<>(25));
                    Cell cell = new Cell(p, mapComps.get(p));
                    mapRows.get(p.y).add(cell);
                    mapCols.get(p.x).add(cell);
                }
                int cellHeight = 0, cellWidth = 0;
                for (java.util.List<Cell> comps : mapRows.values()) {
                    for (Cell cell : comps) {
                        cellWidth = Math.max(cellWidth, cell.component.getPreferredSize().width);
                        cellHeight = Math.max(cellHeight, cell.component.getPreferredSize().height);
                    }
                }
                prefSize = new Dimension(Math.max(cellHeight, cellWidth) * mapCols.size(), Math.max(cellHeight, cellWidth) * mapRows.size());
            }
            Dimension getPreferredSize() {
                return prefSize;
            }
            class Cell {
                final Point point;
                final Component component;
                Cell(Point p, Component c) {
                    point = p;
                    component = c;
                }
            }
        }
    }
}