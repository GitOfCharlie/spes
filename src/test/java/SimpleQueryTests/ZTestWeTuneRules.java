package SimpleQueryTests;

import AlgeNode.AlgeNode;
import AlgeNodeParser.AlgeNodeParserPair;
import AlgeRule.AlgeRule;
import com.microsoft.z3.Context;
import org.apache.calcite.rel.RelNode;

import java.io.*;
import java.util.*;

public class ZTestWeTuneRules {
    public static void markNum() throws Exception{
        File file = new File("src/test/java/SimpleQueryTests/sql/substitutions2.probing");
        File file2 = new File("src/test/java/SimpleQueryTests/sql/substitutions3.probing");
//        System.out.println(file.getAbsolutePath());
        BufferedReader bfReader = new BufferedReader(new FileReader(file));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(file2));
        String line;
        int count = 0;
        while((line = bfReader.readLine()) != null){
            if(line.startsWith("===")) line = line + count++;
            bfWriter.write(line + "\n");
        }
    }

    static Map<String, List<Integer>> statistics = new HashMap<>();

    static{
        statistics.put("eq", new ArrayList<>());
        statistics.put("neq", new ArrayList<>());
        statistics.put("unknown", new ArrayList<>());
        statistics.put("err", new ArrayList<>());
    }

    public static void main(String[] args) throws  Exception{
//        markNum();
        File file = new File("src/test/java/SimpleQueryTests/sql/substitutions0.probing");
        File eqFile = new File("src/test/java/SimpleQueryTests/sql/eq.txt");
        File neqFile = new File("src/test/java/SimpleQueryTests/sql/neq.txt");
        BufferedReader bfReader = new BufferedReader(new FileReader(file));
//        BufferedWriter eqWriter = new BufferedWriter(new FileWriter(eqFile));
//        BufferedWriter neqWriter = new BufferedWriter(new FileWriter(neqFile));
        String line = bfReader.readLine(), sql1, sql2;
        String res;
        List<String> schemas = new ArrayList<>();
        int count = 0;

//        Set<Integer> blackList = Set.of(11, 73);
        Set<Integer> blackList = new HashSet<>();
        blackList.add(11);blackList.add(73);

        while(true){
            res = "";
            sql1 = bfReader.readLine();
            sql2 = bfReader.readLine();
            schemas.clear();
            while((line = bfReader.readLine()) != null){
                if(line.startsWith("===")) break;
                schemas.add(line);
            }

            if(blackList.contains(count)) {
                count++;
                continue;
            }

            System.out.println(count);
            System.out.println(sql1);
            System.out.println(sql2);

            for(String schema : schemas){
                if(schema.contains("UNIQUE") || schema.contains("NOT NULL") || schema.contains("REFERENCES")){
                    res = "unknown";
                    break;
                }
            }

            if(!res.equals("unknown")) {
                List<List<String>> schemaInfo = new ArrayList<>();
                for(String schema: schemas){
                    List<String> cols = fetchCols(schema);
                    schemaInfo.add(cols);
                }
                try {
                    res = prove(sql1, sql2, schemaInfo);
                }catch (Exception e){
                    e.printStackTrace();
                    res = "err";
                }
            }
            statistics.get(res).add(count);

//            if(res.equals("eq")) recordResult(sql1, sql2, schemas, eqWriter, count);
//            else if(res.equals("neq")) recordResult(sql1, sql2, schemas, neqWriter, count);

            count++;

            if(line == null) break;
        }
        System.out.println(statistics);
        System.out.println("eq " + statistics.get("eq").size());
        System.out.println("neq " + statistics.get("neq").size());
        System.out.println("unknown " + statistics.get("unknown").size());
        System.out.println("err " + statistics.get("err").size());
    }

    public static void recordResult(String sql1, String sql2, List<String> schemas, BufferedWriter writer, int count)
    throws IOException{
        writer.write("===" + count + "\n");
        writer.write(sql1 + "\n");
        writer.write(sql2 + "\n");
        for(String schema: schemas){
            writer.write(schema + "\n");
        }
        writer.flush();
    }

    public static List<String> fetchCols(String schema){
        assert schema.startsWith("CREATE TABLE");

        List<String> cols = new ArrayList<>();
        String colInfo = schema.substring(schema.indexOf('(') + 1, schema.indexOf(')'));
        String[] colList = colInfo.split(", ");
        for(String col: colList){
            String colName = col.split(" ")[0];
            if(colName.startsWith("`")) colName = colName.substring(1);
            if(colName.endsWith("`")) colName = colName.substring(0, colName.length() - 1);
            cols.add(colName);
        }
        return cols;
    }

    public static String prove(String sql1, String sql2, List<List<String>> schemaInfo) throws Exception{
        Context z3Context = new Context();
        simpleParser.addDynamicTableSchema(schemaInfo);

        simpleParser parser = new simpleParser();
        RelNode newNode = parser.getRelNode(sql1);
        AlgeNode algeExpr = AlgeRule.normalize(AlgeNodeParserPair.constructAlgeNode(newNode,z3Context));
        System.out.println("first one");
        System.out.println(algeExpr);
        simpleParser parser2 = new simpleParser();
        RelNode newNode2 = parser2.getRelNode(sql2);
        AlgeNode algeExpr2 = AlgeRule.normalize(AlgeNodeParserPair.constructAlgeNode(newNode2,z3Context));
        System.out.println("second one");
        System.out.println(algeExpr2);
        return algeExpr.isEq(algeExpr2) ? "eq" : "neq";
    }

    public static void simpleTest() throws Exception{
        String sql1 = "((SELECT \"q0\".\"a0\" AS \"a0\" FROM \"t0\" AS \"q0\") UNION ALL (SELECT \"q2\".\"a2\" AS \"a2\" FROM \"t1\" AS \"q2\"))";
        String sql2 = "(SELECT \"q0\".\"a0\" AS \"a0\" FROM \"t0\" AS \"q0\")";
        List<List<String>> schemaInfo = new ArrayList<>();
        List<String> tbl1 = new ArrayList<>(); tbl1.add("a0");
        List<String> tbl2 = new ArrayList<>(); tbl2.add("a2");
        schemaInfo.add(tbl1); schemaInfo.add(tbl2);
        System.out.println(prove(sql1, sql2, schemaInfo));
    }
}
