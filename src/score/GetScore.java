package score;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


//run this
public class GetScore {
	public static void main(String[] args) throws Exception, IOException {
		//创建httpClient对象，对cookie进行设置
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();  
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		
		//构造get方法
		HttpGet httpGet = new HttpGet("http://idas.uestc.edu.cn/authserver/login?service=http%3A%2F%2Fportal.uestc.edu.cn%2F");
        httpGet.setHeader("Accept","text/html, application/xhtml+xml, image/jxr, */*");  
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");  
        httpGet.setHeader("Connection","Keep-Alive");  
        httpGet.setHeader("host","idas.uestc.edu.cn");  
        httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");  
   	 	httpGet.setHeader("Upgrade-Insecure-Requests","1");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        
        //执行get方法得到cookie
        CloseableHttpResponse response = httpClient.execute(httpGet); 
        String cookie1 = setCookie(response);//保存得到的cookie
        
        //将响应文本放进字符串(一开始我准备放进文本用htmlcleaner解析html,但是并不是规范的html,效果不是很好)
        HttpEntity responseEntity = response.getEntity();  
        String responseHtml = EntityUtils.toString(responseEntity);
        
        //从响应中得到lt,execution2个动态参数(用的正则表达式截取字符串)
        MatchTool mTool = new MatchTool();
        List<String> list = mTool.match(responseHtml, "input", "value");
        StringBuffer sBuffer1 = new StringBuffer(list.get(2));
        String lt = sBuffer1.substring(0, sBuffer1.length()-2);
        StringBuffer sBuffer2 = new StringBuffer(list.get(4));
        String execution = sBuffer2.substring(0, sBuffer2.length()-2);
        response.close();
        
        //构造post方法
        List<NameValuePair> params = new ArrayList<NameValuePair>();
		HttpPost httpPost = new HttpPost("http://idas.uestc.edu.cn/authserver/login?service=http://portal.uestc.edu.cn/index.portal");
		httpPost.setHeader("Cookie", cookie1);//将get得到的cookie放进去
        httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
   	 	httpPost.setHeader("Host", "idas.uestc.edu.cn");
   	 	httpPost.setHeader("Upgrade-insecure-Requests","1");
		params.add(new BasicNameValuePair("username", "2014220402027"));
        params.add(new BasicNameValuePair("password", "950826xxmh"));
        params.add(new BasicNameValuePair("lt", lt));
        params.add(new BasicNameValuePair("execution", execution));
        params.add(new BasicNameValuePair("dllt", "userNamePasswordLogin"));
        params.add(new BasicNameValuePair("_eventId", "submit"));
        params.add(new BasicNameValuePair("rmShown", "1"));
        String result = "";
        httpPost.setEntity(new UrlEncodedFormEntity(params,Consts.UTF_8));
        
        //执行post得到结果
        HttpResponse httpResponse1 = httpClient.execute(httpPost); 
        if(httpResponse1.getStatusLine().getStatusCode() == 302)  
                 {  
                     HttpEntity httpEntity = httpResponse1.getEntity();  
                     result = EntityUtils.toString(httpEntity);//取出应答字符串  
                     System.out.println(result);//為空
                  }
        
        Header[] loca = httpResponse1.getAllHeaders();
        for(Header loca1 : loca){
       	 if(loca1.getName().equals("Location"))
       	  System.out.println(loca1.getValue());
        }
        
        //查询成绩
         HttpGet g = new HttpGet("http://eams.uestc.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR");
         //得到post请求返回的cookie信息
         String c = setCookie(httpResponse1);

         //将cookie注入到get请求头当中
         g.setHeader("Cookie",c);
         CloseableHttpResponse r = httpClient.execute(g);
         String content = EntityUtils.toString(r.getEntity());
         
         r.close();
         File uestc = new File("uestc.html");  
       	 PrintWriter pw1 = new PrintWriter(uestc, "UTF-8");  
       	 pw1.println(content);  
       	 pw1.close();  
		
	}
	
	//从response中得到cookie(方法来自一篇登录知乎的案例博客)
	public static Map<String,String> cookieMap = new HashMap<String, String>(64);
    public static String setCookie(HttpResponse httpResponse)
    {
        System.out.println("----setCookieStore");
        Header headers[] = httpResponse.getHeaders("Set-Cookie");
        if (headers == null || headers.length==0)
        {
            System.out.println("----there are no cookies");
            return null;
        }
        String cookie = "";
        for (int i = 0; i < headers.length; i++) {
            cookie += headers[i].getValue();
            if(i != headers.length-1)
            {
                cookie += ";";
            }
        }

        String cookies[] = cookie.split(";");
        for (String c : cookies)
        {
            c = c.trim();
            if(cookieMap.containsKey(c.split("=")[0]))
            {
                cookieMap.remove(c.split("=")[0]);
            }
            cookieMap.put(c.split("=")[0], c.split("=").length == 1 ? "":(c.split("=").length ==2?c.split("=")[1]:c.split("=",2)[1]));
        }
        System.out.println("----setCookieStore success");
        String cookiesTmp = "";
        for (String key :cookieMap.keySet())
        {
            cookiesTmp +=key+"="+cookieMap.get(key)+";";
        }

        return cookiesTmp.substring(0,cookiesTmp.length()-2);
    }
}
