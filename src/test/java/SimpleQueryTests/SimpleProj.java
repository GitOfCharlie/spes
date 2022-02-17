package SimpleQueryTests;

import AlgeNode.AlgeNode;
import AlgeNodeParser.AlgeNodeParserPair;
import com.microsoft.z3.Context;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;

public class SimpleProj {
    public static void main(String[] args) throws  Exception{
        Context z3Context = new Context();
        simpleParser parser = new simpleParser();
        RelNode newNode = parser.getRelNode("SELECT DISTINCT SAL FROM EMP");
        AlgeNode algeExpr = AlgeNodeParserPair.constructAlgeNode(newNode,z3Context);
        simpleParser parser2 = new simpleParser();
        RelNode newNode2 = parser2.getRelNode("SELECT SAL FROM EMP");
        AlgeNode algeExpr2 = AlgeNodeParserPair.constructAlgeNode(newNode2,z3Context);
        System.out.println(algeExpr.isEq(algeExpr2));
        //SimpleQueryTests.simpleParser parser2 = new SimpleQueryTests.simpleParser();
        //parser2.explain("SELECT * FROM EMP");
    }
}
