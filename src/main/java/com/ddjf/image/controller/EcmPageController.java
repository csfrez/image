package com.ddjf.image.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ddjf.image.model.EcmPage;
import com.ddjf.image.service.EcmPageService;
import com.ddjf.image.util.FileToZip;
import com.ddjf.image.util.FileUtil;
import com.ddjf.image.util.SpringContextUtil;

import tk.mybatis.mapper.util.StringUtil;

@Controller
@RequestMapping("/DDPM_IMAGEPAG")
public class EcmPageController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EcmPageService ecmPageService;

	/**
	 * 获取列表数据
	 * @param ecmPage
	 * @return
	 */
	@RequestMapping(value = "/findList", produces = "application/json; charset=utf-8")
	@ResponseBody
	public List<EcmPage> findList(EcmPage ecmPage) {
		return ecmPageService.findList(ecmPage);
	}
	
	/**
	 * 获取影像统计信息列表
	 * @param objectNo
	 * @return
	 */
	@RequestMapping(value = "/servlet/ImageList", produces = "application/json; charset=utf-8")
	@ResponseBody
	public List<Map<String, Object>> getImageList(String objectNo) {
		String[] array = objectNo.split("\\|");
		List<String> list = Arrays.asList(array);
		return ecmPageService.getImageList(list);
	}
	
	/**
	 * 获取单个订单详细列表
	 * @param objectType
	 * @param objectNo
	 * @param fileType
	 * @param typeNo
	 * @return
	 */
	@RequestMapping(value = "/ViewImageServlet", produces = "application/json; charset=utf-8")
	@ResponseBody
	public List<Map<String, Object>> getEcmPageList(String objectType, String objectNo, String fileType, String typeNo) {
		EcmPage ecmPage = new EcmPage();
		ecmPage.setObjecttype(objectType);
		ecmPage.setObjectno(objectNo);
		ecmPage.setPagetype(fileType);
		ecmPage.setTypeno(typeNo);
		return ecmPageService.getEcmPageList(ecmPage);
	}
	
	/**
	 * 影像管理处理
	 * @param serialNo
	 * @param type
	 * @param sortNo
	 * @param typeNo
	 * @param angle
	 * @param readFlag
	 * @return
	 */
	@RequestMapping(value = "/servlet/ManageImage")
	@ResponseBody
	public String manageEcmPage(String serialNo, String type, String sortNo, String typeNo, String angle, String readFlag){
		logger.warn("影像管理,serialNo="+serialNo+";type="+type+";typeNo="+typeNo+";sortNo="+sortNo+";angle="+angle+";readFlag="+readFlag);
		String flag ;
		try{
			flag = ecmPageService.manageEcmPage(serialNo, type, sortNo, typeNo, angle, readFlag);
		} catch (Exception e){
			flag = "Fail";
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 删除指定typeno下的影像
	 * @param objectNo
	 * @param typeNo
	 * @param objectType
	 * @param fileType
	 * @return
	 */
	@RequestMapping(value = "/deleteByTypeno")
	@ResponseBody
	public String deleteByTypeno(String objectNo, String typeNo, String objectType, String fileType){
		logger.warn("影像删除处理,objectNo="+objectNo+";typeNo="+typeNo+";objectType="+objectType+";fileType="+fileType);
		String flag = "Fail";
		try{
			if(StringUtil.isEmpty(objectNo)){
				return "objectNo can't empty";
			}
			if(StringUtil.isEmpty(typeNo)){
				return "typeNo can't empty";
			}
			if(StringUtil.isEmpty(objectType)){
				objectType = "CBCreditApply";
			}
			if(StringUtil.isEmpty(fileType)){
				fileType = "Image";
			}
			flag = ecmPageService.deleteByTypeno(objectNo, typeNo, objectType, fileType);
		} catch (Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	
	/**
	 * 影像复制处理
	 * @param fromNo
	 * @param toNo
	 * @param typeNo
	 * @param objectType
	 * @param fileType
	 * @return
	 */
	@RequestMapping(value = "/copyImage")
	@ResponseBody
	public String copyImage(String fromNo, String toNo, String typeNo, String objectType, String fileType){
		logger.warn("影像复制处理,fromNo="+fromNo+";toNo="+toNo+";typeNo="+typeNo+";objectType="+objectType+";fileType="+fileType);
		String flag = "Fail";
		try{
			if(StringUtil.isEmpty(fromNo)){
				return "from applyNo can't empty";
			}
			if(StringUtil.isEmpty(toNo)){
				return "to applyNo can't empty";
			}
			if(StringUtil.isEmpty(typeNo)){
				return "typeNo can't empty";
			}
			if(StringUtil.isEmpty(objectType)){
				objectType = "CBCreditApply";
			}
			if(StringUtil.isEmpty(fileType)){
				fileType = "Image";
			}
			flag = ecmPageService.copyImage(fromNo, toNo, typeNo, objectType, fileType);
		} catch (Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	
	/**
	 * 批量扫描
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/servlet/ActiveX", method = RequestMethod.POST)
	@ResponseBody
	public String batchScan(HttpServletRequest request) {
		try{
			//String ids = request.getParameter("Ids");
			String objectType = request.getParameter("ObjectType");
			String objectNo = request.getParameter("ObjectNo");
			String typeNo = request.getParameter("TypeNo");
			String images = request.getParameter("Images");
			String userId = request.getParameter("UserId");
			String orgId = request.getParameter("OrgId");
			//String method = request.getParameter("Method");
			String imageType = request.getParameter("ImageType");
			logger.warn("批量扫描,objectType="+objectType+";objectNo="+objectNo+";typeNo="+typeNo);
			return ecmPageService.batchScan(userId, orgId, images, imageType, objectNo, objectType, typeNo);
		}catch(Exception e){
			e.printStackTrace();
			return "Fail";
		}
	}

	/**
	 * 文件上传
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/fileUpload")
	@ResponseBody
	public String fileUpload(HttpServletRequest request) {
		try {
			String fileName = request.getParameter("fileName");
			String objectType = request.getParameter("objectType");
			String objectNo = request.getParameter("objectNo");
			String typeNo = request.getParameter("typeNo");
			String userId = request.getParameter("userId");
			String orgId = request.getParameter("orgId");
			String pageType = request.getParameter("pageType");
			String pageName = request.getParameter("pageName");
			InputStream ins = request.getInputStream();
			logger.warn("文件上传,fileName="+fileName+";objectType="+objectType+";objectNo="+objectNo+";typeNo="+typeNo+";inputStream="+ins.available());
			if(StringUtil.isEmpty(fileName)){
				return "fileName can't empty";
			}
			if(StringUtil.isEmpty(objectType)){
				return "objectType can't empty";
			}
			if(StringUtil.isEmpty(objectNo)){
				return "objectNo can't empty";
			}
			if(StringUtil.isEmpty(typeNo)){
				return "typeNo can't empty";
			}
			if(ins.available()<=0){
				//return "InputStream is not empty";
			}
			String documentId = ecmPageService.fileUpload(objectType, objectNo, typeNo, userId, pageType, pageName, fileName, orgId, ins);
			if(StringUtil.isNotEmpty(documentId)){
				return documentId;
			}else {
				return "Fail";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}

	/**
	 * 影像文件查看
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/fileview")
	public void fileView(HttpServletRequest request, HttpServletResponse response) {
		Runtime rt = Runtime.getRuntime();
		InputStream inStream = null;
		ServletOutputStream outStream = null;
		try {
			HttpSession session = request.getSession(true);
			if ((session == null) || (session.getAttributeNames() == null)) {
				throw new Exception("------Timeout------");
			}
			String sFileName = request.getParameter("filename");
			String sViewType = request.getParameter("viewtype");
			String sContentType = request.getParameter("contenttype");
			logger.warn("查看影像开始，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB，文件名，" + sFileName);
			if(StringUtil.isEmpty(sFileName)){
				return;
			}
			if(StringUtil.isEmpty(sContentType)){
				sContentType = "";
			}
			File file = null;
			file = new File(sFileName);
			if (!file.exists()) {
				sFileName = new String(sFileName.getBytes("ISO-8859-1"), "GBK");
				file = new File(sFileName);
			}
			outStream = response.getOutputStream();
			if (file != null && !file.exists()) {
				String sCon = "文件不存在 !";
				outStream.write(sCon.getBytes(Charset.forName("GBK")));
			} else {
				String sNewFileName = FileUtil.getFileName(sFileName);
				String browName = sNewFileName;
				String clientInfo = request.getHeader("User-agent");
				if ((clientInfo != null) && (clientInfo.indexOf("MSIE") > -1)) {
					if ((clientInfo.indexOf("MSIE 6") > -1) || (clientInfo.indexOf("MSIE 5") > -1)) {
						browName = new String(sNewFileName.getBytes("GBK"), "ISO-8859-1");
					} else {
						browName = URLEncoder.encode(sNewFileName, "UTF-8");
					}
				}
				response.setContentType(sContentType + "; charset=UTF-8");
				if ("view".equals(sViewType))
					response.setHeader("Content-Disposition", "filename=" + browName + ";");
				else {
					response.setHeader("Content-Disposition", "attachment;filename=" + browName + ";");
				}
				inStream = new FileInputStream(sFileName);
				int iContentLength = (int) file.length();
				if ((iContentLength < 0) || (iContentLength > 102400)) {
					iContentLength = 102400;
				}
				byte[] bytes = new byte[iContentLength];
				int k = -1;
				while ((k = inStream.read(bytes, 0, iContentLength)) > 0) {
					outStream.write(bytes, 0, k);
				}
			}
			outStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.gc();
			logger.warn("查看影像结束，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB" );
		}
	}
	
	/**
	 * 文件下载
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/DownFile")
	public void downFile(HttpServletRequest request, HttpServletResponse response) {
		Runtime rt = Runtime.getRuntime();
		InputStream inStream = null;
		ServletOutputStream outStream = null;
		try {
			String objectno =request.getParameter("ObjectNo");
			String objecttype =request.getParameter("ObjectType");
			String productType =request.getParameter("ProductType");
			String typeno = request.getParameter("TypeNo");
			logger.warn("下载影像开始，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB，typeNo=" + typeno);

			if(StringUtil.isEmpty(objectno)){
				return ;
			}
			EcmPage ecmPage = new EcmPage();
			ecmPage.setObjecttype(objecttype);
			ecmPage.setObjectno(objectno);
			ecmPage.setTypeno(typeno);
			List<EcmPage> ecmPageList = ecmPageService.findList(ecmPage);
			outStream = response.getOutputStream();
			if(ecmPageList.isEmpty()){
			   String sCon = "该申请下无影像信息 !";
			   outStream.write(sCon.getBytes(Charset.forName("GBK")));;
			} else {
				StringBuilder sb = new StringBuilder();
				if(StringUtil.isNotEmpty(productType)){
					ecmPageService.formatFileName(ecmPageList, productType, sb);
				} else {
					for(EcmPage e: ecmPageList){
						sb.append(e.getDocumentid()).append("@");
					}
				}
				String zipFilePath = SpringContextUtil.getProperty("image.zipFilePath");
				String sourceFilePath = sb.toString().substring(0, sb.length()-1);
				String zipName = objectno+"_"+String.valueOf(System.currentTimeMillis()) + ".zip";
				boolean flag = FileToZip.fileToZip4j(sourceFilePath, zipFilePath, zipName);
				if(flag){
					//创建要下载的文件的对象(参数为要下载的文件在服务器上的路径)
					File serverFile = new File(zipFilePath+"/"+zipName);
					//设置要显示在保存窗口的文件名，如果文件名中有中文的话，则要设置字符集，否则会出现乱码。另外，要写上文件后缀名
					String fileName = URLEncoder.encode(zipName, "UTF-8");
					//该步是最关键的一步，使用setHeader()方法弹出"是否要保存"的对话框，打引号的部分都是固定的值，不要改变
					response.setHeader("Content-disposition","attachment; filename="+fileName);
					/*
					 * 以下四行代码经测试似乎可有可无，可能是我测试的文件太小或者其他什么原因。。。
					 */
					response.setContentType("application/zip");
					//定义下载文件的长度 /字节
					long fileLength = serverFile.length();
					//把长整形的文件长度转换为字符串
					String length = String.valueOf(fileLength);
					//设置文件长度(如果是Post请求，则这步不可少)
					response.setHeader("content_Length", length);
					//获得一个 ServletOutputStream(向客户端发送二进制数据的输出流)对象
					//ServletOutputStream servletOutPutStream = response.getOutputStream();
					//获得一个从服务器上的文件myFile中获得输入字节的输入流对象
					inStream = new FileInputStream(serverFile);
					byte[] bytes = new byte[1024];//设置缓冲区为1024个字节，即1KB
					int len = 0;
					//读取数据。返回值为读入缓冲区的字节总数,如果到达文件末尾，则返回-1
					while((len=inStream.read(bytes)) > 0){   
						//将指定 byte数组中从下标 0 开始的 len个字节写入此文件输出流,(即读了多少就写入多少)
						outStream.write(bytes, 0, len); 
					}
				}else{
					logger.warn("保存［"+objectno+"］申请下的影像信息保存到［"+zipFilePath+"］目录下失败！");
					outStream.write("保存影像失败！".getBytes(Charset.forName("GBK")));;
				}
		   	}
			outStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.gc();
			logger.warn("下载影像结束，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB" );
		}
	}
}
