package SimpleQueryTests;

import AlgeNode.AlgeNode;
import AlgeNodeParser.AlgeNodeParserPair;
import com.microsoft.z3.Context;
import org.apache.calcite.rel.RelNode;

import static AlgeRule.AlgeRule.normalize;

public class simpleAgg {
    public static void main(String[] args) throws  Exception{
        Context z3Context = new Context();
        simpleParser parser = new simpleParser();
        RelNode newNode = parser.getRelNode("SELECT DEPTNO,MGR,max(SAL) FROM EMP group by DEPTNO,MGR having max(SAL) > 10");
        // RelNode newNode = parser.getRelNode("SELECT COMM, COUNT(SAL) FROM (SELECT EMPNO, MGR, COMM, SAL FROM EMP WHERE MGR=1) GROUP BY COMM HAVING COUNT(SAL)=0");

        AlgeNode algeExpr = normalize(AlgeNodeParserPair.constructAlgeNode(newNode,z3Context));
        simpleParser parser2 = new simpleParser();
        // RelNode newNode2 = parser2.getRelNode("SELECT DISTINCT max(SAL) FROM EMP WHERE DEPTNO < 10 group by DEPTNO,MGR");
        RelNode newNode2 = parser2.getRelNode("SELECT COMM, COUNT(SAL) FROM (SELECT * FROM EMP WHERE SAL=1) GROUP BY COMM HAVING COUNT(SAL)=0");
        AlgeNode algeExpr2 = normalize(AlgeNodeParserPair.constructAlgeNode(newNode2,z3Context));
        System.out.println(algeExpr.isEq(algeExpr2));
        //SimpleQueryTests.simpleParser parser2 = new SimpleQueryTests.simpleParser();
        //parser2.explain("SELECT * FROM EMP");
    }
}
