package sec01.test01;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
 
public class BoardDAO { //BoardService 클래스에서 BoardDAO의 select..()메서드 호출하면 계층형 SQL문 실행
    private DataSource dataFactory;
    Connection con;
    PreparedStatement pstmt;
    
    public BoardDAO() {
        try {
            Context ctx = new InitialContext();
            Context envContext = (Context)ctx.lookup("java:/comp/env");
            dataFactory = (DataSource)envContext.lookup("jdbc/oracle");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public List selectAllArticle() {
        List articlesList = new ArrayList();
        try {
            con = dataFactory.getConnection();
            String query = "SELECT LEVEL,articleNO,parentNO,title,content,id,writeDate" 
                     + " from t_board"
                     + " START WITH  parentNO=0" + " CONNECT BY PRIOR articleNO=parentNO"
                     + " ORDER SIBLINGS BY articleNO DESC";
        System.out.println(query); //계층형 SQL문
        pstmt = con.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            int level = rs.getInt("level"); // 각 글의 깊이를 level속성에 저장
            int articleNO = rs.getInt("articleNO"); // 글 번호는 숫자형이므로 getint() 사용
            int parentNO = rs.getInt("parentNO");
            String title = rs.getString("title");
            String content = rs.getString("content");
            String id = rs.getString("id");
            Date writeDate = rs.getDate("writeDate");
            
            ArticleVO article = new ArticleVO();
            article.setLevel(level);
            article.setArticleNO(articleNO);
            article.setParentNO(parentNO);
            article.setTitle(title);
            article.setContent(content);
            article.setId(id);
            article.setWriteDate(writeDate); //위의 글 정보를 ArticleVO 객체의 속성에 설정
            articlesList.add(article); 
            }
            rs.close();
            pstmt.close();
            con.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return articlesList;
    }
   
    
    
    private int getNewArticleNO() {
        try {
            con = dataFactory.getConnection();
            String query = "SELECT  max(articleNO) from t_board ";
            System.out.println(query);
            pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery(query);
            if (rs.next())
                return (rs.getInt(1) + 1); //큰 번호에 1을 더한 번호를 반환
            rs.close();
            pstmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    } //기본 글 번호중 가장 큰 번호를 조회하기 위함
    
    public int insertNewArticle(ArticleVO article) {
        int articleNO = getNewArticleNO(); // 새글에 대한 글 번호를 가져옴
        try {
            con = dataFactory.getConnection();
            int parentNO = article.getParentNO();
            String title = article.getTitle();
            String content = article.getContent();
            String id = article.getId();
            String imageFileName = article.getImageFileName();
            String query = "INSERT INTO t_board (articleNO, parentNO, title, content, imageFileName, id)"
                    + " VALUES (?, ? ,?, ?, ?, ?)";
            System.out.println(query);
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, articleNO);
            pstmt.setInt(2, parentNO);
            pstmt.setString(3, title);
            pstmt.setString(4, content);
            pstmt.setString(5, imageFileName);
            pstmt.setString(6, id);
            pstmt.executeUpdate();
            pstmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articleNO;
    }

	public ArticleVO selectAllArticle(int articleVO) {
		ArticleVO article = new ArticleVO();
		try {
			con=dataFactory.getConnection();
			String query = "SELECT articleNO, parentNO, title, content, imageFileName, id, writeDate"
					+" from t_board"
					+" where articleNO=?";
			
			System.out.println(query);
			pstmt=con.prepareStatement(query);
			pstmt.setInt(1, articleVO);
			ResultSet rs=pstmt.executeQuery();
			rs.next();
			
			int _articleNO = rs.getInt("articleNO");
			int parentNO = rs.getInt("parentNO");
			String title = rs.getString("title");
			String content = rs.getString("content");
			String imageFileName = rs.getString("imageFileName");
			String id = rs.getString("id");
			Date writeDate = rs.getDate("writeDate");
			
			article.setArticleNO(_articleNO);
			article.setParentNO(parentNO);
			article.setTitle(title);
			article.setContent(content);
			article.setImageFileName(imageFileName);
			article.setId(id);
			article.setWriteDate(writeDate);
			
			rs.close();
			pstmt.close();
			con.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return article;
	}
}