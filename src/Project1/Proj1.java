package Project1;

import java.sql.*;
import java.util.ArrayList;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.OneWayAnova;

public class Proj1 {
    //Part II
    static String query1A = "SELECT COUNT(p_id) FROM patient "
            + "WHERE p_id IN (SELECT p_id FROM Diagnosis WHERE ds_id IN "
            + "(SELECT ds_id FROM disease "
            + "WHERE name LIKE '%tumor%' OR type LIKE '%tumor%' OR description LIKE '%tumor%'))";
    static String query1B = "SELECT COUNT(p_id) FROM patient "
            + "WHERE p_id IN (SELECT p_id FROM Diagnosis WHERE ds_id IN "
            + "(SELECT ds_id FROM disease WHERE type = 'leukemia'))";
    static String query1C = "SELECT COUNT(p_id) FROM patient "
            + "WHERE p_id IN (SELECT p_id FROM Diagnosis WHERE ds_id IN "
            + "(SELECT ds_id FROM disease WHERE name = 'ALL'))";

    static String query2A = "CREATE TABLE IF NOT EXISTS cse601.temp (dr_id VARCHAR(8));";
    static String query2B = "INSERT INTO temp SELECT DISTINCT dr_id FROM DrugUse "
            + "WHERE p_id IN (SELECT p_id FROM Diagnosis WHERE ds_id IN "
            + "(SELECT ds_id FROM disease WHERE name LIKE '%tumor%' OR type LIKE '%tumor%' OR description LIKE '%tumor%'));";
    static String query2C = "SELECT DISTINCT type FROM drug INNER JOIN temp ON drug.dr_id = temp.dr_id;";

    static String query3A = "CREATE TABLE IF NOT EXISTS cse601.temp1 (s_id VARCHAR(10));";
    static String query3B = "CREATE TABLE IF NOT EXISTS cse601.temp2 (s_id VARCHAR(10));";
    static String query3C = "INSERT INTO temp1 SELECT DISTINCT s_id FROM "
            + "(SELECT DISTINCT mf.pb_id, mf.s_id FROM microarray_fact mf, probe p "
            + "WHERE mf.mu_id = '001' AND mf.pb_id = p.pb_id AND p.UID IN ("
            + "SELECT UID FROM gene_fact WHERE cl_id = '00002')) AS T";
    static String query3D = "INSERT INTO temp2 SELECT DISTINCT mf.s_id FROM microarray_fact mf, clinical_fact cf "
            + "WHERE mf.s_id = cf.s_id AND cf.p_id IN (SELECT p_id FROM Diagnosis "
            + "WHERE ds_id IN (SELECT ds_id FROM disease WHERE name LIKE '%tumor%' OR type LIKE '%tumor%' OR description LIKE '%tumor%'));";
    static String query3E = "SELECT DISTINCT s_id, exp FROM microarray_fact WHERE s_id IN "
            + "(SELECT temp1.s_id FROM temp1, temp2 WHERE temp1.s_id = temp2.s_id);";

    static String query4A = "SELECT exp from temp4;";
    static String query4B = "SELECT exp from temp6;";

    //temp8 for "ALL"; temp10 for "AML'; temp12 for "Colon
    //tumor"; temp14 for "Breast tumor"
    static String query5A = "SELECT exp from temp8;";
    static String query5B = "SELECT exp from temp10;";
    static String query5C = "SELECT exp from temp12;";
    static String query5D = "SELECT exp from temp14;";

    //PartIII
    //use temp27, temp28
    static String queryIII1A = "CREATE TABLE temp26 (UID VARCHAR(10), s_id VARCHAR(10), exp INT(11));";
    static String queryIII1B = "INSERT INTO temp26 SELECT p.UID, mf.s_id, mf.exp "
            + "FROM probe p, microarray_fact mf WHERE p.pb_id = mf.pb_id;";
    static String queryIII1C = "CREATE TABLE temp20 (p_id VARCHAR(8));";
    static String queryIII1D = "INSERT INTO temp20 SELECT p_id FROM patient WHERE p_id IN ("
            + "SELECT p_id FROM Diagnosis WHERE ds_id IN ("
            + "SELECT ds_id FROM disease WHERE name = 'ALL'));";
    static String queryIIIE = "CREATE TABLE temp27 (UID VARCHAR(10), s_id VARCHAR(10), exp INT(11));";
    static String queryIIIF = "INSERT INTO temp27 SELECT UID, s_id, exp FROM temp26 WHERE s_id IN ("
            + "SELECT DISTINCT s_id FROM clinical_fact WHERE p_id IN ("
            + "SELECT p_id FROM temp20));";
    static String queryIIIG = "CREATE TABLE temp23 (p_id VARCHAR(8));";
    static String queryIIIH = "INSERT INTO temp23 SELECT p_id FROM patient WHERE p_id IN ("
            + "SELECT p_id FROM Diagnosis WHERE ds_id IN ("
            + "SELECT ds_id FROM disease WHERE name <> 'ALL'));";
    static String queryIIIJ = "CREATE TABLE temp28 (UID VARCHAR(10), s_id VARCHAR(10), exp INT(11));";
    static String queryIIIK = "INSERT INTO temp28 SELECT UID, s_id, exp FROM temp26 WHERE s_id IN ("
            + "SELECT DISTINCT s_id FROM clinical_fact WHERE p_id IN ("
            + "SELECT p_id FROM temp23));";
    
    
    public static void main(String[] args){
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cse601", "root", "123456");
            statement = connection.createStatement();
            
            System.out.println("Query 1A is \n" + query1A);
            rs = statement.executeQuery(query1A);
            System.out.println("Execution 1A successful");
            if(rs.next()){
                System.out.println("Number of patients who had tumor is " + rs.getString(1));
            }
            System.out.println("\nQuery 1B is \n" + query1B);
            rs = statement.executeQuery(query1B);
            System.out.println("Execution 1B successful");
            if(rs.next()){
                System.out.println("Number of patients who had leukemia is " + rs.getString(1));
            }
            System.out.println("\nQuery 1C is \n" + query1C);
            rs = statement.executeQuery(query1C);
            System.out.println("Execution 1C successful");
            if(rs.next()){
                System.out.println("Number of patients who had ALL is " + rs.getString(1));
            }
            
            System.out.println("\n\nStarting query 2:");
            statement.executeUpdate(query2A);
            System.out.println("\nCreate temp table successful");
            System.out.println("\nQuery is 2B\n" + query2B);
            System.out.println("\nQuery is 2C\n" + query2C);
            statement.executeUpdate(query2B);
            System.out.println("\temp table populated successful\n");
            rs = statement.executeQuery(query2C);
            System.out.println("Execution of Query 2 successful\n");
            System.out.println("List of types of drugs applied to patients with \"tumor\"");
            int count = 0;
            while(rs.next()){
                System.out.println(rs.getString(1));
                count++;
            }
            System.out.println("Total number of types of drugs are " + count);

            System.out.println("\n\nStarting query 3:");
            // statement.executeUpdate(query3A);
            // statement.executeUpdate(query3B);
            // System.out.println("\nCreate temp tables successful");
            //
            // statement.executeUpdate(query3C);
            // System.out.println("\ntemp1 table populated successfuly\n");
            // statement.executeUpdate(query3D);
            // System.out.println("\ntemp2 table populated successfuly\n");
            rs = statement.executeQuery(query3E);
//            System.out.println("\nList of mRNA values:\n");
            count = 0;
            int z = 0;
            while(rs.next()){
//                System.out.println(rs.getString(1) + "\t" + rs.getString(2));
                count++;
//                z++;
//                if(z == 20){
//                    break;
//                }
            }
            System.out.println("Total number of query3 is " + count);

            System.out.println("\n\nStarting query 4:");
            ArrayList<Double> list1 = new ArrayList<Double>();
            ArrayList<Double> list2 = new ArrayList<Double>();
            rs = statement.executeQuery(query4A);
            while(rs.next()){
                list1.add(Double.parseDouble(rs.getString(1)));
            }
            int i = 0;
            double[] result1 = new double[list1.size()];
            for (Double d : list1) {
                result1[i++] = d.doubleValue();
            }
            System.out.println("size of result1 " + result1.length);
            rs = statement.executeQuery(query4B);
            while(rs.next()){
                list2.add(Double.parseDouble(rs.getString(1)));
            }
            i = 0;
            double[] result2 = new double[list2.size()];
            for (Double d : list2) {
                result2[i++] = d.doubleValue();
            }
            System.out.println("size of result2 " + result2.length);
            Double t = new TTest().t(result1, result2);
            System.out.println("t test result is " + t);

            // query 5
            // temp8 for "ALL"; temp10 for "AML'; temp12 for "Colon
            // tumor"; temp14 for "Breast tumor"
            System.out.println("\n\nStarting query 5:");
            ArrayList<String> queries = new ArrayList<String>();
            queries.add(query5A);
            queries.add(query5B);
            queries.add(query5C);
            queries.add(query5D);
            ArrayList<double[]> collection = new ArrayList<double[]>();
            int j;
            for(i = 0; i < 4; i++){
                rs = statement.executeQuery(queries.get(i));
                ArrayList<Double> list = new ArrayList<Double>();
                while(rs.next()){
                    list.add(Double.parseDouble(rs.getString(1)));
                }
                double[] result = new double[list.size()];
                System.out.println("list " + i + " size is " + list.size());
                j = 0;
                for(Double d : list){
                    result[j++] = d.doubleValue();
                }
                collection.add(result);
            }
            double anovaF = new OneWayAnova().anovaFValue(collection);
            double anovaP = new OneWayAnova().anovaPValue(collection);
            System.out.println("F-value for query5 " + anovaF);
            System.out.println("P-value for query5 " + anovaP);
            
            // PartIII
            System.out.println("\n\n***************PART III");
            ArrayList<ArrayList<Double>> groupA = new ArrayList<ArrayList<Double>>(); // from temp27
            ArrayList<ArrayList<Double>> groupB = new ArrayList<ArrayList<Double>>(); // from temp28
            Double expression;
            String geneID = null;
            rs = statement.executeQuery("SELECT UID FROM probe ORDER BY UID LIMIT 1");
            if(rs.next()){
                geneID = rs.getString(1);
            }
            // System.out.println("First UID " + geneID + " FOR GroupA");
            rs = statement.executeQuery("SELECT * FROM temp27 ORDER BY UID");
            String last = "";
            list1 = new ArrayList<Double>();
            groupA.add(list1);
            while(rs.next()){
                last = geneID;
                geneID = rs.getString(1);
                expression = Double.parseDouble(rs.getString(3));
                if(geneID.equals(last)){
                    list1.add(expression);
                }
                else{
                    list1 = new ArrayList<Double>();
                    list1.add(expression);
                    groupA.add(list1);
                }
            }
            // System.out.println("GroupA SIZE " + groupA.size());
            // System.out.println("GroupA1'SIZE " + groupA.get(0).size());
            // for(Double d : groupA.get(0)){
            // System.out.println(d);
            // }

            rs = statement.executeQuery("SELECT UID FROM probe ORDER BY UID LIMIT 1");
            if(rs.next()){
                geneID = rs.getString(1);
            }
            // System.out.println("First UID " + geneID + " FOR GroupB");
            rs = statement.executeQuery("SELECT * FROM temp28 ORDER BY UID");
            last = "";
            list2 = new ArrayList<Double>();
            groupB.add(list2);
            while(rs.next()){
                last = geneID;
                geneID = rs.getString(1);
                expression = Double.parseDouble(rs.getString(3));
                if(geneID.equals(last)){
                    list2.add(expression);
                }
                else{
                    list2 = new ArrayList<Double>();
                    list2.add(expression);
                    groupB.add(list2);
                }
            }
            // System.out.println("GroupB SIZE " + groupB.size());
            // System.out.println("GroupB1'SIZE " + groupB.get(0).size());
            //
            // for(Double d : groupB.get(0)){
            // System.out.println(d);
            // }
            int a, b;
            Double test;
            ArrayList<Integer> geneNumber = new ArrayList<Integer>();
            ArrayList<String> geneUID = new ArrayList<String>();
            int informativeGeneCount = 0;
            for(i = 0; i < groupA.size(); i++){
                a = 0;
                b = 0;
                double[] arrayA = new double[groupA.get(i).size()];
                double[] arrayB = new double[groupB.get(i).size()];
                for(Double d : groupA.get(i)){
                    arrayA[a++] = d.doubleValue();
                }
                for(Double d : groupB.get(i)){
                    arrayB[b++] = d.doubleValue();
                }
                test = new TTest().tTest(arrayA, arrayB);
                if(test - 0.01 < 0){
                    informativeGeneCount++;
                    geneNumber.add(i);
                }
            }
            System.out.println("Total number of informative genes is " + informativeGeneCount);
            rs = statement.executeQuery("SELECT UID FROM probe ORDER BY UID");
            count = 0;
            j = 0;
            while(rs.next()){
                if(count == geneNumber.get(j)){
                    geneUID.add(rs.getString(1));
                    j++;
                }
                count++;
                if (j == geneNumber.size()) {
                    break;
                }
            }
            System.out.println("\nInformative genes UID list as follows:\n");
            for(String s : geneUID){
                System.out.println(s);
            }
        }catch(ClassNotFoundException e){
            System.out.println("Error: " + e.getMessage());
        }catch(SQLException e){
            System.out.println("Error: " + e.getMessage());
        }finally{
            if(connection != null){
                try{
                    connection.close();
                }catch(SQLException e){

                }
            }
            if(statement != null){
                try{
                    statement.close();
                }catch(SQLException e){

                }
            }
        }
    }
}

class SampleExpression {
    String s_id;
    Double exp;

    public SampleExpression(String s_id, Double exp) {
        this.s_id = s_id;
        this.exp = exp;
    }
}
