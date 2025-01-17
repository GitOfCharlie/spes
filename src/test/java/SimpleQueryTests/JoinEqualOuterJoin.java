package SimpleQueryTests;

import AlgeNode.AlgeNode;
import AlgeNodeParser.AlgeNodeParserPair;
import AlgeRule.AlgeRule;
import com.microsoft.z3.Context;
import org.apache.calcite.rel.RelNode;

public class JoinEqualOuterJoin {
    public static void main(String[] args) throws  Exception{
        // "SELECT * FROM EMP LEFT JOIN DEPT ON EMP.DEPTNO = DEPT.DEPTNO WHERE DEPT.NAME IS NULL"
        // "SELECT * FROM EMP INNER JOIN (SELECT * FROM DEPT WHERE NAME IS NULL ) as B on EMP.DEPTNO = B.DEPTNO"
        // THEY ARE NOT EQUAL
        Context z3Context = new Context();
        simpleParser parser = new simpleParser();
        RelNode newNode = parser.getRelNode("SELECT * FROM EMP LEFT JOIN DEPT ON EMP.DEPTNO = DEPT.DEPTNO WHERE DEPT.DEPTNO < 5");
        AlgeNode algeExpr = AlgeRule.normalize(AlgeNodeParserPair.constructAlgeNode(newNode,z3Context));
        System.out.println(algeExpr);
        simpleParser parser2 = new simpleParser();
        RelNode newNode2 = parser2.getRelNode("SELECT * FROM EMP INNER JOIN (SELECT * FROM DEPT WHERE DEPTNO < 5 ) as B on EMP.DEPTNO = B.DEPTNO ");
        AlgeNode algeExpr2 = AlgeRule.normalize(AlgeNodeParserPair.constructAlgeNode(newNode2,z3Context));
        System.out.println("second one");
        System.out.println(algeExpr2);
        System.out.println(algeExpr.isEq(algeExpr2));
        //SimpleQueryTests.simpleParser parser2 = new SimpleQueryTests.simpleParser();
        //parser2.explain("SELECT * FROM EMP");
    }

}
