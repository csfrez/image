package com.ddjf.image.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.ddjf.image.mapper.EcmPageMapper;
import com.ddjf.image.model.EcmPage;
import com.ddjf.image.qrcode.BufferedImageHandler;
import com.ddjf.image.qrcode.PsImage;
import com.ddjf.image.qrcode.QRCodeDecoderHandler;
import com.ddjf.image.util.Constant;
import com.ddjf.image.util.DateUtil;
import com.ddjf.image.util.FileToZip;
import com.ddjf.image.util.FileUtil;
import com.ddjf.image.util.SpringContextUtil;
import com.ddjf.image.util.UniqueIdUtil;
import com.github.pagehelper.PageHelper;

import tk.mybatis.mapper.util.StringUtil;


/**
 * 
 * @author Frez
 *
 */
@Service
public class EcmPageService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass()); 

    @Autowired
    private EcmPageMapper ecmPageMapper;

    public List<EcmPage> getAll(EcmPage ecmPage) {
        if (ecmPage.getPage() != null && ecmPage.getRows() != null) {
            PageHelper.startPage(ecmPage.getPage(), ecmPage.getRows());
        }
        return ecmPageMapper.findList(ecmPage);
    }
    
    /**
     * 获取列表数据
     * @param ecmPage
     * @return
     */
    public List<EcmPage> findList(EcmPage ecmPage) {
        return ecmPageMapper.findList(ecmPage);
    }
    
    /**
     * 影像统计列表
     * @param list
     * @return
     */
    public List<Map<String, Object>> getImageList(List<String> list){
    	return ecmPageMapper.getImageList(list);
    }
    
    /**
     * 获取单个影像列表
     * @param ecmPage
     * @return
     */
    public List<Map<String, Object>> getEcmPageList(EcmPage ecmPage){
    	return ecmPageMapper.getEcmPageList(ecmPage);
    }
    
    /**
     * 文件管理处理
     * @param serialNo
     * @param type
     * @param sortNo
     * @param typeNo
     * @param angle
     * @param readFlag
     * @return
     */
    public String manageEcmPage(String serialNo, String type, String sortNo, String typeNo, String angle, String readFlag){
    	String[] array = serialNo.split("\\|");
    	String result = "Sucess";
    	if("DeleteImage".equals(type)){
			this.deleteImage(array);
		}else if("UpdateTypeNo".equals(type)){
			this.updateTypeNo(array, typeNo);
		}else if("UpdateSortNo".equals(type)){
			this.updateSortNo(array, sortNo);
		} else if("GetImage".equals(type)){

		} else if("PsImage".equals(type)){
			result = this.psImage(array, Integer.parseInt(angle));
		} else if("UpdateReadFlag".equals(type)){
			this.updateReadFlag(array, readFlag);
		} else {
			result = "方法不存在！";
		}
    	return result;
    }
    
    /**
     * 更新文件排序号
     * @param array
     * @param sortNo
     */
    public void updateSortNo(String[] array, String sortNo){
    	EcmPage ecmPage = new EcmPage();
    	for(String serialno : array){
    		ecmPage.setSerialno(serialno);
    		ecmPage.setSortno(sortNo);
    		ecmPageMapper.updateEcmPage(ecmPage);
    	}
    }
    
    /**
     * 更新文件typeNo
     * @param array
     * @param typeNo
     */
    public void updateTypeNo(String[] array, String typeNo){
    	EcmPage ecmPage = new EcmPage();
    	for(String serialno : array){
    		ecmPage.setSerialno(serialno);
    		ecmPage.setTypeno(typeNo);
    		ecmPageMapper.updateEcmPage(ecmPage);
    	}
    }
    
    /**
     * 更新文件已读未读
     * @param array
     * @param readFlag
     */
    public void updateReadFlag(String[] array, String readFlag){
    	EcmPage ecmPage = new EcmPage();
    	for(String serialno : array){
    		ecmPage.setSerialno(serialno);
    		ecmPage.setReadflag(readFlag);
    		ecmPageMapper.updateEcmPage(ecmPage);
    	}
    }
	
	/**
	 * 删除文件
	 * @param array
	 */
	public void deleteImage(String[] array){
		logger.warn("deleteImage=" + array);
		for(String serialno : array){
			ecmPageMapper.deleteEcmPage(serialno);
		}
	}
	
	/**
	 * 旋转文件
	 * @param array
	 * @param angle
	 * @return
	 */
	public String psImage(String[] array, int angle){
		try {
			if(angle <=0 ){
				return "angle参数错误";
			}
			for(String serialno : array){
				EcmPage ecmPage = ecmPageMapper.getBySerialno(serialno);
				File image = new File(ecmPage.getDocumentid());
				if (image.isFile() && image.exists()) {
					PsImage psImage = new PsImage(image);
					psImage.rotate(angle).createPic(null);
				}
			}
			return "Success";
		} catch (Exception e) {
			return "Fail";
		}
	}
	
	public String deleteByTypeno(String objectNo, String typeNo, String objectType, String fileType){
		EcmPage ecmPage = new EcmPage();
		ecmPage.setObjectno(objectNo);
		ecmPage.setTypeno(typeNo);
		ecmPage.setObjecttype(objectType);
		ecmPage.setPagetype(fileType);
		List<EcmPage> ecmPageList = this.findList(ecmPage);
		for(EcmPage model : ecmPageList){
			ecmPageMapper.deleteEcmPage(model.getSerialno());
		}
		return "Success";
	}
	
	/**
	 * 影像复制处理 
	 * @param fromNo
	 * @param toNo
	 * @param typeNo
	 * @param objectType
	 * @param fileType
	 * @return
	 * @throws Exception
	 */
	public String copyImage(String fromNo, String toNo, String typeNo, String objectType, String fileType) throws Exception{
		EcmPage ecmPage = new EcmPage();
		ecmPage.setObjectno(fromNo);
		ecmPage.setTypeno(typeNo);
		ecmPage.setObjecttype(objectType);
		ecmPage.setPagetype(fileType);
		String folderStr = SpringContextUtil.getProperty("image.fileFolder");
		List<EcmPage> ecmPageList = this.findList(ecmPage);
		for(EcmPage model : ecmPageList){
			File image = new File(model.getDocumentid());
			if (image.isFile() && image.exists()){
				String filePath = folderStr + Constant.SEPARATE_SLASH + DateUtil.formate("yyyy/MMdd") + Constant.SEPARATE_SLASH + toNo;
				FileInputStream fis = new FileInputStream(image);
				String documentId = FileUtil.insToFile(fis, filePath, image.getName());
				if(StringUtil.isNotEmpty(documentId)){
					String serialNo = UniqueIdUtil.getUniqueId();
					ecmPage = this.assembleEcmPage(serialNo, toNo, model.getObjecttype(), typeNo, documentId, model.getPagetype(), model.getImageinfo(), model.getPagename(), model.getOperateuser(), model.getOperateorg());
					ecmPageMapper.insertEcmPage(ecmPage);
				}
			}
		}
		return "Success";
	}
    
    /**
     * 批量扫描文件处理
     * @param userId
     * @param orgId
     * @param images
     * @param imageType
     * @param objectNo
     * @param objectType
     * @param typeNo
     * @return
     */
    public String batchScan(String userId, String orgId, String images, String imageType, String objectNo, String objectType, String typeNo){
		Runtime rt = Runtime.getRuntime();
		logger.warn("开始批量扫描影像，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB，图片大小："+images.length()/(1024)+"KB");
		String[] imgs = images.split("\\|");
		String imageInfo = "batchScan"; 
		String folderStr = SpringContextUtil.getProperty("image.fileFolder");
		for(int i=0, len=imgs.length; i<len; i++){
			String base64String = imgs[i].replace(" ", "+");
			byte[] bytes = Base64.decodeBase64(base64String);
			String serialNo = UniqueIdUtil.getUniqueId();
			String fileName = serialNo + ".jpg";
			String filePath = folderStr + Constant.SEPARATE_SLASH + DateUtil.formate("yyyy/MMdd") + Constant.SEPARATE_SLASH + objectNo;
			BufferedImage bufImg = BufferedImageHandler.getSubBufferedImage(bytes);
			String result = QRCodeDecoderHandler.decoderQRCode(bufImg);
			logger.warn("result=" + result);
			String code;
			if(StringUtil.isNotEmpty(result) && result.startsWith("{")){
				JSONObject json = JSONObject.parseObject(result);
				code = json.getString("code");
				String type = json.getString("type");
				if("02".equals(type)){
					continue;
				}
			}else{
				code = result;
			}
			if(StringUtil.isNotEmpty(code)){
				typeNo = code;
			}
			String documentId = FileUtil.bytesToFile(bytes, filePath, fileName);
			EcmPage ecmPage = this.assembleEcmPage(serialNo, objectNo, objectType, typeNo, documentId, "Image", imageInfo, fileName, userId, orgId);
			ecmPageMapper.insertEcmPage(ecmPage);
			System.gc();
		}
		logger.warn("批量扫描影像结束，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB");
		return "Success";
	}
    
    /**
     * 文件上传处理
     * @param objectType
     * @param objectNo
     * @param typeNo
     * @param userId
     * @param pageType
     * @param pageName
     * @param fileName
     * @param orgId
     * @param ins
     * @return
     */
    public String fileUpload(String objectType, String objectNo, String typeNo, String userId, String pageType, String pageName, String fileName, String orgId, InputStream ins){
    	Runtime rt = Runtime.getRuntime();
    	logger.warn("上传影像开始，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB");
    	String imageInfo = "Upload"; 
    	String folderStr = SpringContextUtil.getProperty("image.fileFolder");
		String serialNo = UniqueIdUtil.getUniqueId();
		String filePath = folderStr + Constant.SEPARATE_SLASH + DateUtil.formate("yyyy/MMdd") + Constant.SEPARATE_SLASH + objectNo;
		String documentId = FileUtil.insToFile(ins, filePath, fileName);
		if(StringUtil.isNotEmpty(documentId)){
			if(StringUtil.isEmpty(pageType)){
				pageType = "Image";
			}
			EcmPage ecmPage = this.assembleEcmPage(serialNo, objectNo, objectType, typeNo, documentId, pageType, imageInfo, pageName, userId, orgId);
			ecmPageMapper.insertEcmPage(ecmPage);
		}
		System.gc();
		logger.warn("上传影像结束，内存总量:"+rt.totalMemory()/(1024)+"KB，内存剩余："+rt.freeMemory()/(1024)+"KB，documentId="+documentId);
    	return documentId;
    }
    
    /**
     * 组装EcmPage对象
     * @param serialno
     * @param objectno
     * @param objecttype
     * @param typeno
     * @param documentid
     * @param pagetype
     * @param imageinfo
     * @param pagename
     * @param operateuser
     * @param operateorg
     * @return
     */
    private EcmPage assembleEcmPage(String serialno, String objectno, String objecttype, String typeno, String documentid, String pagetype, String imageinfo, 
    		String pagename, String operateuser, String operateorg){
    	EcmPage ecmPage = new EcmPage();
    	ecmPage.setObjecttype(objecttype);
    	ecmPage.setObjectno(objectno);
    	ecmPage.setTypeno(typeno);
    	int pageNum = this.getMaxPageNum(objecttype, objectno, typeno);
    	if(pageNum > 0){
    		pageNum = pageNum + 1;
    	} else {
    		pageNum = 1;
    	}
    	int sortNum = this.getMaxSortNo(objecttype, objectno);
    	if(sortNum > 0){
    		sortNum =  sortNum + 10;
    	} else {
    		sortNum = 10;
    	}
    	ecmPage.setSerialno(serialno);
    	ecmPage.setPagenum(Integer.toString(pageNum));
    	ecmPage.setSortno(Integer.toString(sortNum));
    	ecmPage.setDocumentid(documentid);
    	ecmPage.setPagetype(pagetype);
    	ecmPage.setImageinfo(imageinfo);
    	ecmPage.setPagename(pagename);
    	ecmPage.setModifytime(DateUtil.formate("yyyy/MM/dd HH:mm:ss"));
    	ecmPage.setOperateuser(operateuser);
    	ecmPage.setOperateorg(operateorg);
    	return ecmPage;
    }

    /**
     * 根据序列号获取ECMPage对象
     * @param serialno
     * @return
     */
    public EcmPage getBySerialno(String serialno) {
        return ecmPageMapper.getBySerialno(serialno);
    }
    
    /**
     * 获取最大PageNum
     * @param objecttype
     * @param objectno
     * @param typeno
     * @return
     */
    public int getMaxPageNum(String objecttype, String objectno, String typeno){
    	EcmPage ecmPage = new EcmPage();
    	ecmPage.setObjecttype(objecttype);
    	ecmPage.setObjectno(objectno);
    	ecmPage.setTypeno(typeno);
    	Integer number = ecmPageMapper.getMaxPageNum(ecmPage);
    	if(number != null){
    		return number;
    	} else {
    		return 0;
    	}
    }
    
    /**
     * 获取最大SortNo
     * @param objecttype
     * @param objectno
     * @return
     */
    public int getMaxSortNo(String objecttype, String objectno){
    	EcmPage ecmPage = new EcmPage();
    	ecmPage.setObjecttype(objecttype);
    	ecmPage.setObjectno(objectno);
    	Integer number = ecmPageMapper.getMaxSortNo(ecmPage);
    	if(number != null){
    		return number;
    	} else {
    		return 0;
    	}
    }
    
    /**
     * 格式化zip文件类容列表
     * @param ecmPageList
     * @param productType
     * @param sb
     */
    public void formatFileName(List<EcmPage> ecmPageList, String productType, StringBuilder sb){
    	List<Map<String, String>> typeList = ecmPageMapper.getEcmPageTypeList(productType);
    	Map<String, String> typeMap = new HashMap<>();
    	for(Map<String, String> type : typeList){
    		typeMap.put(type.get("typeno"), type.get("typename"));
    	}
    	Map<String, Integer> record = new ConcurrentHashMap<>();
    	for(EcmPage ecmPage : ecmPageList){
    		String sTypeNo = ecmPage.getTypeno();
			String sDocumentID = ecmPage.getDocumentid();
			String path = sDocumentID.substring(0, sDocumentID.lastIndexOf(Constant.SEPARATE_SLASH)) + Constant.SEPARATE_SLASH;
			String suffix = sDocumentID.substring(sDocumentID.lastIndexOf(Constant.SEPARATE_DOT));
			Integer count = record.get(sTypeNo);
			String sTypeName = typeMap.get(sTypeNo);
			if(StringUtil.isNotEmpty(sTypeName)){
				if(count != null && count > 0){
					count = count + 1;
				} else {
					count = 1;
				}
				String dest = path + sTypeName+Constant.SEPARATE_UNDERLINE+count+suffix;
				FileToZip.fileChannelCopy(sDocumentID, dest);
				sb.append(dest).append(Constant.SEPARATE_AT);
				record.put(sTypeNo, count);
			}else{
				sb.append(sDocumentID).append(Constant.SEPARATE_AT);
			}
    	}
    }

    public void save(EcmPage ecmPage) {
        if (StringUtil.isNotEmpty(ecmPage.getSerialno())) {
            ecmPageMapper.updateByPrimaryKey(ecmPage);
        } else {
        	ecmPageMapper.insertEcmPage(ecmPage);
        }
    }
}
