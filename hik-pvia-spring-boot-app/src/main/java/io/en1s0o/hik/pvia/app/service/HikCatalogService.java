package io.en1s0o.hik.pvia.app.service;

import io.en1s0o.common.http.GetRequest;
import io.en1s0o.common.http.PostRequest;
import io.en1s0o.common.http.Request;
import io.en1s0o.hik.pvia.starter.domain.vo.PVIAObjectVO;
import io.en1s0o.hik.pvia.starter.service.HikPVIAService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 海康目录服务
 *
 * @author En1s0o
 */
@Service
@SuppressWarnings("unused")
public class HikCatalogService {

    private static final Object SYNC = new Object();

    private final HikPVIAService hikPVIAService;

    public HikCatalogService(HikPVIAService hikPVIAService) {
        this.hikPVIAService = hikPVIAService;
    }

    /**
     * 获取基础目录列表
     * 其中，常用目录为 国标目录，注意区分区域类型
     *
     * <p>/xresmgr-web/catalog/getBasicCatalogList.do，返回示例：
     * <p>{"type":0,"code":"0","msg":"SUCCESS","data":{"list":[{"catalogIndexCode":"10","catalogName":"国标目录"},{"catalogIndexCode":"30","catalogName":"司法目录","children":[{"catalogIndexCode":"31","catalogName":"部级平台","children":[{"catalogIndexCode":"3101","catalogName":"司法部"},{"catalogIndexCode":"3102","catalogName":"司法部监狱管理局"},{"catalogIndexCode":"3103","catalogName":"司法部戒毒管理局"},{"catalogIndexCode":"3108","catalogName":"司法部矫正管理局"},{"catalogIndexCode":"3112","catalogName":"燕城监狱"}]},{"catalogIndexCode":"32","catalogName":"省级平台","children":[{"catalogIndexCode":"3201","catalogName":"省司法厅"},{"catalogIndexCode":"3202","catalogName":"省监狱管理局"},{"catalogIndexCode":"3203","catalogName":"省戒毒管理局"},{"catalogIndexCode":"3208","catalogName":"省矫正管理局"}]},{"catalogIndexCode":"33","catalogName":"地方平台","children":[{"catalogIndexCode":"3302","catalogName":"地方监狱平台"},{"catalogIndexCode":"3303","catalogName":"地方戒毒平台"},{"catalogIndexCode":"3308","catalogName":"地方社区矫正平台"}]},{"catalogIndexCode":"34","catalogName":"新疆兵团平台","children":[{"catalogIndexCode":"3401","catalogName":"新疆兵团"},{"catalogIndexCode":"3402","catalogName":"兵团监狱管理局"},{"catalogIndexCode":"3403","catalogName":"兵团戒毒管理局"},{"catalogIndexCode":"3408","catalogName":"兵团矫正管理局"},{"catalogIndexCode":"3412","catalogName":"兵团监狱平台"},{"catalogIndexCode":"3413","catalogName":"兵团戒毒所"},{"catalogIndexCode":"3418","catalogName":"兵团司法局"}]}]},{"catalogIndexCode":"40","catalogName":"模板导入目录"},{"catalogIndexCode":"50","catalogName":"自定义目录"}]}}
     *
     * @return 基础目录列表
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO getBasicCatalogList() throws Exception {
        Request request = new GetRequest("/xresmgr-web/catalog/getBasicCatalogList.do");
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 获取业务目录列表
     *
     * <p>/xresmgr-web/catalog/getCatalogList.do，返回示例：
     * <p>{"type":0,"code":"0","msg":"SUCCESS","data":{"isImpShow":false,"isRootEdit":false,"list":[{"catalogIndexCode":"10","catalogName":"广东省","catalogGroup":0,"level":3,"organType":"00"}]}}
     *
     * @return 业务目录列表
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO getCatalogList() throws Exception {
        Request request = new GetRequest("/xresmgr-web/catalog/getCatalogList.do");
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 获取 关联目录标识 和 关联资源类型
     * 在创建或更新业务目录时用到
     *
     * <p>/xresmgr-web/catalog/findResourceTypeAndIdentityList.do，返回示例：
     * <p>{"type":0,"code":"0","msg":"SUCCESS","data":{"identity":[{"value":"布控报警业务目录","key":"balarm_basic_tree","resourceType":"DEFENCE@CROSS@IO@DETECTOR@CAMERA@WITNESS_DEVICE@ALARM_DEVICE@"},{"value":"视频应用业务目录","key":"bvideo_basic_tree","resourceType":"DEFENCE@CROSS@IO@DETECTOR@CAMERA@WITNESS_DEVICE@ALARM_DEVICE@"},{"value":"人脸应用业务目录","key":"iface_basic_tree","resourceType":"DEFENCE@CROSS@IO@DETECTOR@CAMERA@WITNESS_DEVICE@ALARM_DEVICE@"},{"value":"电子地图业务目录","key":"imap_basic_tree","resourceType":"DEFENCE@CROSS@IO@DETECTOR@CAMERA@WITNESS_DEVICE@ALARM_DEVICE@"},{"value":"智能车辆应用业务目录","key":"ivehicle_basic_tree","resourceType":"DEFENCE@CROSS@IO@DETECTOR@CAMERA@WITNESS_DEVICE@ALARM_DEVICE@"},{"value":"无线终端应用业务目录","key":"iwifi_basic_tree","resourceType":"DEFENCE@CROSS@IO@DETECTOR@CAMERA@WITNESS_DEVICE@ALARM_DEVICE@"}],"resourceType":[{"value":"报警设备","key":"ALARM_DEVICE"},{"value":"监控点","key":"CAMERA"},{"value":"卡口","key":"CROSS"},{"value":"防区","key":"DEFENCE"},{"value":"探针","key":"DETECTOR"},{"value":"编码设备","key":"ENCODE_DEVICE"},{"value":"报警输入/输出","key":"IO"},{"value":"车底扫描通道","key":"vehBottom"},{"value":"车底扫描设备","key":"vehDetectDevice"},{"value":"人证设备","key":"WITNESS_DEVICE"}]}}
     *
     * @return 关联目录标识列表 和 关联资源类型列表
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO findResourceTypeAndIdentityList() throws Exception {
        Request request = new GetRequest("/xresmgr-web/catalog/findResourceTypeAndIdentityList.do");
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 创建或更新业务目录
     *
     * @param catalogName   目录名称
     * @param regionName    区域名称
     * @param resourceTypes 资源类型列表。resourceType 之间使用 "@" 分割：
     *                      如果 tags = ["CROSS", "ALARM_DEVICE", "WITNESS_DEVICE"]
     *                      那么传递到海康为 "CROSS@ALARM_DEVICE@WITNESS_DEVICE"
     * @param identities    目录标识列表。identity 之间使用 "@" 分割：
     *                      如果 tags = ["iface_basic_tree", "iwifi_basic_tree", "bvideo_basic_tree"]
     *                      那么传递到海康为 "iface_basic_tree@iwifi_basic_tree@bvideo_basic_tree"
     * @return 创建或更新业务目录的结果
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO addOrUpdateBusinessCatalog(
            String catalogName,
            String regionName,
            Collection<String> resourceTypes,
            Collection<String> identities) throws Exception {
        Map<String, String> values = new HashMap<>(8);
        values.put("catalogName", catalogName);
        values.put("regionName", regionName);
        values.put("resourceType", String.join("@", resourceTypes));
        values.put("identity", String.join("@", identities));
        Request request = new PostRequest.Form("/xresmgr-web/catalog/addOrUpdateBusinessCatalog.do", values);
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 校验新增区域配置
     *
     * <p>/xresmgr-web/catalog/validateData.do
     * <pre><code>
     *     ------WebKitFormBoundarygxjb9czE24o2HGCg
     *     Content-Disposition: form-data; name="catalogIndexCode"
     *
     *     10
     *     ------WebKitFormBoundarygxjb9czE24o2HGCg
     *     Content-Disposition: form-data; name="_csrf"
     *
     *     0d06927a-4ef0-4d23-a1be-21cd9d1ef4bd
     *     ------WebKitFormBoundarygxjb9czE24o2HGCg
     *     Content-Disposition: form-data; name="upload"; filename="catalogTemplate.csv"
     *     Content-Type: application/vnd.ms-excel
     *
     *
     *     ------WebKitFormBoundarygxjb9czE24o2HGCg--
     * </code></pre>
     *
     * @param catalogIndexCode 目录索引码
     * @return 校验新增区域配置结果
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO validateData(String catalogIndexCode, String csv) throws Exception {
        // import 是导入之前 validateData 校验成功的数据，如果不加锁，可能导致 import 到这里的数据
        synchronized (SYNC) {
            Collection<MultipartBody.Part> parts = new ArrayList<>();
            parts.add(MultipartBody.Part.createFormData("catalogIndexCode", catalogIndexCode));
            parts.add(MultipartBody.Part.createFormData("upload", "catalogTemplate.csv",
                    RequestBody.create(MediaType.get("application/vnd.ms-excel"), csv)));
            Request request = new PostRequest.Multipart(
                    "/xresmgr-web/catalog/validateData.do",
                    null, parts);
            return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
        }
    }

    /**
     * 新增区域编码配置，[警告] 新增区域编码配置很难改掉，需要修改配置文件和数据库，重启服务等
     *
     * <p>/xresmgr-web/catalog/import.do
     * <p>{"type":0,"code":"0","msg":"SUCCESS","data":{"success":1,"total":{"add":0,"total":5,"update":1}}}
     * <p>共5条数据导入成功，更新1个区域编码，新增0个区域编码
     *
     * <p>下面操作添加了新增区域编码配置，但是删除就很难了
     * <pre><code>
     * String csv = "区域外码,区域名称,上级区域外码,区域层级\n" +
     *   "44,广东省,10,1\n" +
     *   "44000000002160000001,test001,44,2\n" +
     *   "44000000002160000100,分公司100,44000000002160000001,3\n" +
     *   "44000000002160000101,分公司101,44000000002160000001,3";
     * return hikCatalogService.importCatalog("10", csv);
     * </code></pre>
     *
     * @return 新增区域编码配置结果
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO importCatalog(String catalogIndexCode, String csv) throws Exception {
        // import 是导入之前 validateData 校验成功的数据，所以需要加锁
        synchronized (SYNC) {
            PVIAObjectVO vo = validateData(catalogIndexCode, csv);
            if ("0".equals(vo.getCode())) {
                Request request = new PostRequest.Json("/xresmgr-web/catalog/import.do", null);
                return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
            } else {
                return vo;
            }
        }
    }

}
