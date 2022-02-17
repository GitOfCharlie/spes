package SimpleQueryTests;

import AlgeNode.AlgeNode;
import AlgeNodeParser.AlgeNodeParserPair;
import AlgeRule.AlgeRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.microsoft.z3.Context;
import org.apache.calcite.rel.RelNode;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZTestCalcitePairs {
    static Map<String, List<Integer>> statistics = new HashMap<>();

    static {
        statistics.put("eq", new ArrayList<>());
        statistics.put("neq", new ArrayList<>());
        statistics.put("err", new ArrayList<>());
    }

    public static void main(String[] args) {
        int count = 1;
        try {
            InputStream stream = new FileInputStream("src/test/java/SimpleQueryTests/sql/calcite_tests.json");
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            Gson gson = new GsonBuilder().create();

            reader.beginArray();
            while (reader.hasNext()) {
                SQLPair pair = gson.fromJson(reader, SQLPair.class);
                System.out.println(count);
                System.out.println(pair.q1);
                System.out.println(pair.q2);
                try {
                    String res = prove(pair.q1, pair.q2);
                    statistics.get(res).add(count);
                }catch (Exception e){
                    statistics.get("err").add(count);
                    e.printStackTrace();
                    count++;
                    continue;
                }
                count++;
            }
            reader.close();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("eq " + statistics.get("eq"));
            System.out.println("neq " + statistics.get("neq"));
            System.out.println("err " + statistics.get("err"));
            System.out.println("eq " + statistics.get("eq").size());
            System.out.println("neq " + statistics.get("neq").size());
            System.out.println("err " + statistics.get("err").size());
        }

    }
    public static String prove(String sql1, String sql2) throws Exception{
        Context z3Context = new Context();
        simpleParser parser = new simpleParser();
        RelNode newNode = parser.getRelNode(sql1);
        AlgeNode algeExpr = AlgeRule.normalize(AlgeNodeParserPair.constructAlgeNode(newNode,z3Context));

        simpleParser parser2 = new simpleParser();
        RelNode newNode2 = parser2.getRelNode(sql2);
        AlgeNode algeExpr2 = AlgeRule.normalize(AlgeNodeParserPair.constructAlgeNode(newNode2,z3Context));

        return algeExpr.isEq(algeExpr2) ? "eq" : "neq";
    }

    private static class SQLPair {
        String name;
        String q1;
        String q2;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getQ1() {
            return q1;
        }

        public void setQ1(String q1) {
            this.q1 = q1;
        }

        public String getQ2() {
            return q2;
        }

        public void setQ2(String q2) {
            this.q2 = q2;
        }

        @Override
        public String toString() {
            return "SQLPair{" + "name=" + name + ", sql1=" + q1 + ", sql2=" + q2 + '}';
        }
    }

}
