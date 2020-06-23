package sec01.test01;

/**
 * Servlet implementation class BoardControll
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
/**
 * Servlet implementation class BoardController
 */
@WebServlet("/board/*")
public class BoardControll extends HttpServlet { //요청시 글 목록 출력의 역할, 
    private static String ARTICLE_IMAGE_REPO = "C:\\board\\image"; //글에 첨부한 이미지 저장 위치를 상수로 변환
    BoardService boardService;
    ArticleVO articleVO;
 
    public void init(ServletConfig config) throws ServletException {
        boardService = new BoardService(); //서블릿 초기화시 BoardService 객체를 생성
        articleVO = new ArticleVO();
    }
 
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)    throws ServletException, IOException {
        doHandle(request, response);
    }
 
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response)    throws ServletException, IOException {
        doHandle(request, response);
    }
 
    private void doHandle(HttpServletRequest request, HttpServletResponse response)    throws ServletException, IOException {
        String nextPage = "";
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        String action = request.getPathInfo(); //요청명을 가져옴
        System.out.println("action:" + action);
        try {
            List<ArticleVO> articlesList = new ArrayList<ArticleVO>();
            if (action == null) {
                articlesList = boardService.listArticles();
                request.setAttribute("articlesList", articlesList);
                nextPage = "/boardtest/listArticles.jsp";
            } 
            else if (action.equals("/listArticles.do")) { // action값에 따라
                articlesList = boardService.listArticles(); // 전체글을 조회
                request.setAttribute("articlesList", articlesList); // 조회한 글을 바인딩한 후 jsp로 포워딩
                nextPage = "/boardtest/listArticles.jsp";
            }
            else if(action.equals("/articleForm.do")) {
                nextPage = "/boardtest/articleForm.jsp";
            }else if(action.equals("/viewArticle.do")) {
            	String articleNO = request.getParameter("articleNO");
            	articleVO = boardService.viewArticle(Integer.parseInt(articleNO));
            	request.setAttribute("article", articleVO);
            	nextPage="/boardtest/viewArticle.jsp";
            }
            //------------------------------ 조회에 관한 내용 ------------------------------------------------------
 
            else if(action.equals("/addArticle.do")) {
                int articleNO=0;
                Map<String, String> articleMap = upload(request, response);
                String title = articleMap.get("title");
                String content = articleMap.get("content");
                String imageFileName = articleMap.get("imageFileName");
                //articleMap에 저장된 글 정보를 다시 가져옴
                articleVO.setParentNO(0);
                articleVO.setId("PARK"); // PK에 있는 ID를 사용해서 그 사용자가 등록하도록 조정
                articleVO.setTitle(title);
                articleVO.setContent(content);
                articleVO.setImageFileName(imageFileName);
                articleNO = boardService.addArticle(articleVO); //테이블에 새 글을 추가한 후 새 글에 대한 글 번호를 가져옴
                if(imageFileName != null && imageFileName.length() != 0) {//파일을 첨부한 경우에만 
                    File srcFile = new File(ARTICLE_IMAGE_REPO+ "\\" + "tep" + "\\" + imageFileName);//tmp폴더에 임시로 업로드된 파일 객체 생성
                    File desDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
                    desDir.mkdirs();//해당 경로에 글 번호로 폴더를 생성한다.
                    FileUtils.moveFileToDirectory(srcFile, desDir, true);//tmp 폴더의 파일을 글 번호를 이름으로 하는 폴더로 이동
                    
                }
                PrintWriter pw = response.getWriter();
                pw.print("<script>" 
                         +"  alert('새글을 추가했습니다.');" 
                         +" location.href='"+request.getContextPath()+"/board/listArticles.do';"
                         +"</script>");
                //새 글 등록 메세지를 나타낸 후 자바스크립트 location객체의 href로 글 목록을 요청.
                return;
                //boardService.addArticle(articleVO); 
                //nextPage = "/board/listArticles.do";
                //글쓰기 창에서 입력된 정보를 ArticleVO객체에 설정한 후 addArticle()로 전달
            }
            RequestDispatcher dispatch = request.getRequestDispatcher(nextPage);
            dispatch.forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Map<String, String> upload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> articleMap = new HashMap<String, String>();
        String encoding = "utf-8";
        File currentDirPath = new File(ARTICLE_IMAGE_REPO); //글 이미지 저장폴더에 대해 파일 객체를 생성
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(currentDirPath);
        factory.setSizeThreshold(1024 * 1024);
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List items = upload.parseRequest(request);
            for (int i = 0; i < items.size(); i++) {
                FileItem fileItem = (FileItem) items.get(i);
                if (fileItem.isFormField()) {
                    System.out.println(fileItem.getFieldName() + "=" + fileItem.getString(encoding));
                    articleMap.put(fileItem.getFieldName(), fileItem.getString(encoding));
                    //파일 업로드로 같이 전송된 새 글 관련 매개변수를 Map<>에 저장한 후 반환
                    //새 글과 관련된 title 등을 map에 저장한다.
                } else {
                    System.out.println("파라미터명:" + fileItem.getFieldName());
                    //System.out.println("파일명:" + fileItem.getName());
                    System.out.println("파일크기:" + fileItem.getSize() + "bytes");
                    //articleMap.put(fileItem.getFieldName(), fileItem.getName());
                    //업로드된 파일의 파일이름을 map에 (imagefilename,업로드 파일이름) 으로 저장
                    if (fileItem.getSize() > 0) {
                        int idx = fileItem.getName().lastIndexOf("\\");
                        if (idx == -1) {
                            idx = fileItem.getName().lastIndexOf("/");
                        }
 
                        String fileName = fileItem.getName().substring(idx + 1);
                        System.out.println("파일명:" + fileName);
                        articleMap.put(fileItem.getFieldName(), fileName);  //익스플로러에서 업로드 파일의 경로 제거 후 map에 파일명 저장
                        File uploadFile = new File(currentDirPath + "\\tep\\" + fileName);
                        fileItem.write(uploadFile);
                        //File uploadFile = new File(currentDirPath + "\\" + fileName);
                        //fileItem.write(uploadFile);
                        //업로드한 파일이 존재하는 경우 업로드한 파일의 파일이름으로 저장소에 업로드 
                    } 
                } 
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articleMap;
    }
 
}
