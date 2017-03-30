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
		//����httpClient���󣬶�cookie��������
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();  
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		
		//����get����
		HttpGet httpGet = new HttpGet("http://idas.uestc.edu.cn/authserver/login?service=http%3A%2F%2Fportal.uestc.edu.cn%2F");
        httpGet.setHeader("Accept","text/html, application/xhtml+xml, image/jxr, */*");  
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");  
        httpGet.setHeader("Connection","Keep-Alive");  
        httpGet.setHeader("host","idas.uestc.edu.cn");  
        httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");  
   	 	httpGet.setHeader("Upgrade-Insecure-Requests","1");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        
        //ִ��get�����õ�cookie
        CloseableHttpResponse response = httpClient.execute(httpGet); 
        String cookie1 = setCookie(response);//����õ���cookie
        
        //����Ӧ�ı��Ž��ַ���(һ��ʼ��׼���Ž��ı���htmlcleaner����html,���ǲ����ǹ淶��html,Ч�����Ǻܺ�)
        HttpEntity responseEntity = response.getEntity();  
        String responseHtml = EntityUtils.toString(responseEntity);
        
        //����Ӧ�еõ�lt,execution2����̬����(�õ�������ʽ��ȡ�ַ���)
        MatchTool mTool = new MatchTool();
        List<String> list = mTool.match(responseHtml, "input", "value");
        StringBuffer sBuffer1 = new StringBuffer(list.get(2));
        String lt = sBuffer1.substring(0, sBuffer1.length()-2);
        StringBuffer sBuffer2 = new StringBuffer(list.get(4));
        String execution = sBuffer2.substring(0, sBuffer2.length()-2);
        response.close();
        
        //����post����
        List<NameValuePair> params = new ArrayList<NameValuePair>();
		HttpPost httpPost = new HttpPost("http://idas.uestc.edu.cn/authserver/login?service=http://portal.uestc.edu.cn/index.portal");
		httpPost.setHeader("Cookie", cookie1);//��get�õ���cookie�Ž�ȥ
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
        
        //ִ��post�õ����
        HttpResponse httpResponse1 = httpClient.execute(httpPost); 
        if(httpResponse1.getStatusLine().getStatusCode() == 302)  
                 {  
                     HttpEntity httpEntity = httpResponse1.getEntity();  
                     result = EntityUtils.toString(httpEntity);//ȡ��Ӧ���ַ���  
                     System.out.println(result);//���
                  }
        
        Header[] loca = httpResponse1.getAllHeaders();
        for(Header loca1 : loca){
       	 if(loca1.getName().equals("Location"))
       	  System.out.println(loca1.getValue());
        }
        
        //��ѯ�ɼ�
         HttpGet g = new HttpGet("http://eams.uestc.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR");
         //�õ�post���󷵻ص�cookie��Ϣ
         String c = setCookie(httpResponse1);

         //��cookieע�뵽get����ͷ����
         g.setHeader("Cookie",c);
         CloseableHttpResponse r = httpClient.execute(g);
         String content = EntityUtils.toString(r.getEntity());
         
         r.close();
         File uestc = new File("uestc.html");  
       	 PrintWriter pw1 = new PrintWriter(uestc, "UTF-8");  
       	 pw1.println(content);  
       	 pw1.close();  
		
	}
	
	//��response�еõ�cookie(��������һƪ��¼֪���İ�������)
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
