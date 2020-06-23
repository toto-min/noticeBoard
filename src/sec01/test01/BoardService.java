package sec01.test01;

import java.util.List;

public class BoardService { // BoardDAO 객체를 생성한후 select...()메서드로 전체글 가져옴
    BoardDAO boardDAO;
    public BoardService() {
        boardDAO = new BoardDAO(); // 생성자 호출시 BoardDAO 객체를 생성
    }
    public List<ArticleVO> listArticles(){
        List<ArticleVO> articleList = boardDAO.selectAllArticle();
        return articleList;
    }
    public int addArticle(ArticleVO article) {
        return boardDAO.insertNewArticle(article);
    }
    
    public ArticleVO viewArticle(int articleVO) {
    	ArticleVO article = null;
    	article = boardDAO.selectAllArticle(articleVO);
    	return article;
    }
}