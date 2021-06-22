package io.en1s0o.hik.pvia.app.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import io.en1s0o.common.http.GetRequest;
import io.en1s0o.common.http.PostRequest;
import io.en1s0o.common.http.Request;
import io.en1s0o.hik.pvia.app.domain.vo.PVIARegionListVO;
import io.en1s0o.hik.pvia.app.domain.vo.PVIARegionVO;
import io.en1s0o.hik.pvia.app.domain.vo.PVIATagListVO;
import io.en1s0o.hik.pvia.starter.domain.vo.PVIAObjectVO;
import io.en1s0o.hik.pvia.starter.service.HikPVIAService;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 海康区域服务
 *
 * @author En1s0o
 */
@Service
@SuppressWarnings("unused")
public class HikRegionService {

    private final HikPVIAService hikPVIAService;
    private final HikManagerService hikManagerService;

    public HikRegionService(HikPVIAService hikPVIAService,
                            HikManagerService hikManagerService) {
        this.hikPVIAService = hikPVIAService;
        this.hikManagerService = hikManagerService;
    }

    /**
     * 获取区域标签列表
     *
     * <p>/xresmgr-web/manager/dict/getTagList.do?type=region
     * <p>{"type":0,"code":"0","msg":"SUCCESS","data":[{"name":"通用","indexCode":"01.01","path":"@01@01.01@","hasLeaf":true,"pIndexCode":"01"},{"name":"位置类型","indexCode":"01.01.01","path":"@01@01.01@01.01.01@","hasLeaf":true,"pIndexCode":"01.01"},{"name":"检查站","indexCode":"01.01.01.01","path":"@01@01.01@01.01.01@01.01.01.01@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"党政机关","indexCode":"01.01.01.02","path":"@01@01.01@01.01.01@01.01.01.02@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"车站码头","indexCode":"01.01.01.03","path":"@01@01.01@01.01.01@01.01.01.03@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"中心广场","indexCode":"01.01.01.04","path":"@01@01.01@01.01.01@01.01.01.04@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"体育场馆","indexCode":"01.01.01.05","path":"@01@01.01@01.01.01@01.01.01.05@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"商业中心","indexCode":"01.01.01.06","path":"@01@01.01@01.01.01@01.01.01.06@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"宗教场所","indexCode":"01.01.01.07","path":"@01@01.01@01.01.01@01.01.01.07@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"校园","indexCode":"01.01.01.08","path":"@01@01.01@01.01.01@01.01.01.08@","hasLeaf":true,"pIndexCode":"01.01.01"},{"name":"幼儿园","indexCode":"01.01.01.08.01","path":"@01@01.01@01.01.01@01.01.01.08@01.01.01.08.01@","hasLeaf":false,"pIndexCode":"01.01.01.08"},{"name":"小学","indexCode":"01.01.01.08.02","path":"@01@01.01@01.01.01@01.01.01.08@01.01.01.08.02@","hasLeaf":false,"pIndexCode":"01.01.01.08"},{"name":"中学","indexCode":"01.01.01.08.03","path":"@01@01.01@01.01.01@01.01.01.08@01.01.01.08.03@","hasLeaf":false,"pIndexCode":"01.01.01.08"},{"name":"大学","indexCode":"01.01.01.08.04","path":"@01@01.01@01.01.01@01.01.01.08@01.01.01.08.04@","hasLeaf":false,"pIndexCode":"01.01.01.08"},{"name":"治安复杂区域","indexCode":"01.01.01.09","path":"@01@01.01@01.01.01@01.01.01.09@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"交通干线","indexCode":"01.01.01.10","path":"@01@01.01@01.01.01@01.01.01.10@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"医院","indexCode":"01.01.01.11","path":"@01@01.01@01.01.01@01.01.01.11@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"金融机构","indexCode":"01.01.01.12","path":"@01@01.01@01.01.01@01.01.01.12@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"危险物品场所","indexCode":"01.01.01.13","path":"@01@01.01@01.01.01@01.01.01.13@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"博物馆展览馆","indexCode":"01.01.01.14","path":"@01@01.01@01.01.01@01.01.01.14@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"重点水域、航道","indexCode":"01.01.01.15","path":"@01@01.01@01.01.01@01.01.01.15@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"农村地区","indexCode":"01.01.01.16","path":"@01@01.01@01.01.01@01.01.01.16@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"高铁沿线","indexCode":"01.01.01.17","path":"@01@01.01@01.01.01@01.01.01.17@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"市际公安检查站","indexCode":"01.01.01.18","path":"@01@01.01@01.01.01@01.01.01.18@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"涉外场所","indexCode":"01.01.01.19","path":"@01@01.01@01.01.01@01.01.01.19@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"边境沿线","indexCode":"01.01.01.20","path":"@01@01.01@01.01.01@01.01.01.20@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"旅游景区","indexCode":"01.01.01.21","path":"@01@01.01@01.01.01@01.01.01.21@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"网吧","indexCode":"01.01.01.22","path":"@01@01.01@01.01.01@01.01.01.22@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"居民小区","indexCode":"01.01.01.23","path":"@01@01.01@01.01.01@01.01.01.23@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"社区","indexCode":"01.01.01.24","path":"@01@01.01@01.01.01@01.01.01.24@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"街道/派出所","indexCode":"01.01.01.25","path":"@01@01.01@01.01.01@01.01.01.25@","hasLeaf":false,"pIndexCode":"01.01.01"},{"name":"其他","indexCode":"01.01.01.99","path":"@01@01.01@01.01.01@01.01.01.99@","hasLeaf":false,"pIndexCode":"01.01.01"}]}
     *
     * @return 区域标签列表
     * @throws Exception 接口请求异常
     */
    public PVIATagListVO getRegionTagList() throws Exception {
        return hikManagerService.getTagList("region");
    }

    /**
     * 获取区域外码列表
     *
     * <p>/xresmgr-web/region/findCodeList.do?catalogIndexCode=10&pRegionExternalCode=10
     * <p>{"type":0,"code":"0","msg":"SUCCESS","data":[{"regionExternalCode":"11","regionName":"北京市","hasLeaf":1},{"regionExternalCode":"12","regionName":"天津市","hasLeaf":1},{"regionExternalCode":"13","regionName":"河北省","hasLeaf":1},{"regionExternalCode":"14","regionName":"山西省","hasLeaf":1},{"regionExternalCode":"15","regionName":"内蒙古自治区","hasLeaf":1},{"regionExternalCode":"21","regionName":"辽宁省","hasLeaf":1},{"regionExternalCode":"22","regionName":"吉林省","hasLeaf":1},{"regionExternalCode":"23","regionName":"黑龙江省","hasLeaf":1},{"regionExternalCode":"31","regionName":"上海市","hasLeaf":1},{"regionExternalCode":"32","regionName":"江苏省","hasLeaf":1},{"regionExternalCode":"33","regionName":"浙江省","hasLeaf":1},{"regionExternalCode":"34","regionName":"安徽省","hasLeaf":1},{"regionExternalCode":"35","regionName":"福建省","hasLeaf":1},{"regionExternalCode":"36","regionName":"江西省","hasLeaf":1},{"regionExternalCode":"37","regionName":"山东省","hasLeaf":1},{"regionExternalCode":"41","regionName":"河南省","hasLeaf":1},{"regionExternalCode":"42","regionName":"湖北省","hasLeaf":1},{"regionExternalCode":"43","regionName":"湖南省","hasLeaf":1},{"regionExternalCode":"44","regionName":"广东省","hasLeaf":1},{"regionExternalCode":"45","regionName":"广西壮族自治区","hasLeaf":1},{"regionExternalCode":"46","regionName":"海南省","hasLeaf":1},{"regionExternalCode":"50","regionName":"重庆市","hasLeaf":1},{"regionExternalCode":"51","regionName":"四川省","hasLeaf":1},{"regionExternalCode":"52","regionName":"贵州省","hasLeaf":1},{"regionExternalCode":"53","regionName":"云南省","hasLeaf":1},{"regionExternalCode":"54","regionName":"西藏自治区","hasLeaf":1},{"regionExternalCode":"61","regionName":"陕西省","hasLeaf":1},{"regionExternalCode":"62","regionName":"甘肃省","hasLeaf":1},{"regionExternalCode":"63","regionName":"青海省","hasLeaf":1},{"regionExternalCode":"64","regionName":"宁夏回族自治区","hasLeaf":1},{"regionExternalCode":"65","regionName":"新疆维吾尔自治区","hasLeaf":1},{"regionExternalCode":"71","regionName":"台湾省","hasLeaf":0},{"regionExternalCode":"81","regionName":"香港特别行政区","hasLeaf":0},{"regionExternalCode":"82","regionName":"澳门特别行政区","hasLeaf":0}]}
     *
     * @param catalogIndexCode    目录索引码
     * @param pRegionExternalCode 上级区域外码
     * @return 区域外码列表
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO findCodeList(String catalogIndexCode, String pRegionExternalCode) throws Exception {
        if (Strings.isNullOrEmpty(catalogIndexCode)) {
            return error();
        }

        StringBuilder sb = new StringBuilder("/xresmgr-web/region/findCodeList.do");
        sb.append("?regionIndexCode=").append(catalogIndexCode.trim());
        if (!Strings.isNullOrEmpty(pRegionExternalCode)) {
            sb.append("&pRegionExternalCode=").append(pRegionExternalCode.trim());
        }

        Request request = new GetRequest(sb.toString());
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 获取可用的区域外码
     *
     * <p>/xresmgr-web/region/getUserDefinedIndexCode.do?catalogIndexCode=10&pRegionExternalCode=10
     * <p>{"type":0,"code":"0","msg":"SUCCESS","data":{"regionExternalCode":"10000000002160000002"}}
     *
     * @param catalogIndexCode    目录索引码
     * @param pRegionExternalCode 上级区域外码，表示添加到哪个区域下
     * @return 可用的区域外码
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO getUserDefinedIndexCode(String catalogIndexCode, String pRegionExternalCode) throws Exception {
        StringBuilder sb = new StringBuilder("/xresmgr-web/region/getUserDefinedIndexCode.do");
        if (!Strings.isNullOrEmpty(catalogIndexCode)) {
            sb.append("?catalogIndexCode=").append(catalogIndexCode.trim());
            if (!Strings.isNullOrEmpty(pRegionExternalCode)) {
                sb.append("&pRegionExternalCode=").append(pRegionExternalCode.trim());
            }
        } else {
            return error();
        }

        Request request = new GetRequest(sb.toString());
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 搜索区域名称
     *
     * @param catalogIndexCode 目录索引码
     * @param regionName       区域名称
     * @return 搜索区域名称结果
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO findByName(String catalogIndexCode, String regionName) throws Exception {
        StringBuilder sb = new StringBuilder("/xresmgr-web/region/findByName.do");
        if (!Strings.isNullOrEmpty(catalogIndexCode)) {
            sb.append("?catalogIndexCode=").append(catalogIndexCode.trim());
            if (!Strings.isNullOrEmpty(regionName)) {
                sb.append("&regionName=").append(regionName.trim());
            }
        } else if (!Strings.isNullOrEmpty(regionName)) {
            sb.append("?regionName=").append(regionName.trim());
        }

        Request request = new GetRequest(sb.toString());
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 获取区域信息列表
     *
     * <p>如果没有提供 <code>catalogIndexCode</code> 和 <code>regionIndexCode</code>，
     * <p>即访问 /xresmgr-web/region/getAsyncRegion.do，表示获取根区域
     * <p>/xresmgr-web/region/getAsyncRegion.do?catalogIndexCode=10，相当于 regionIndexCode=-1
     *
     * <p>当指定 regionIndexCode，且不为 -1 时，可能没有 children 节点（分层获取），但不代表没有 children
     *
     * @param catalogIndexCode 目录索引码（可选），表示获取哪个目录下的区域信息列表
     * @param regionIndexCode  区域索引码（可选），表示获取哪个区域的下级区域信息列表
     * @return 区域信息列表
     * @throws Exception 接口请求异常
     */
    public PVIARegionListVO getAsyncRegion(String catalogIndexCode, String regionIndexCode) throws Exception {
        StringBuilder sb = new StringBuilder("/xresmgr-web/region/getAsyncRegion.do");
        if (!Strings.isNullOrEmpty(catalogIndexCode)) {
            sb.append("?catalogIndexCode=").append(catalogIndexCode.trim());
            if (!Strings.isNullOrEmpty(regionIndexCode)) {
                sb.append("&regionIndexCode=").append(regionIndexCode.trim());
            }
        } else if (!Strings.isNullOrEmpty(regionIndexCode)) {
            sb.append("?regionIndexCode=").append(regionIndexCode.trim());
        }

        Request request = new GetRequest(sb.toString());
        return hikPVIAService.doRequest(request, PVIARegionListVO.class).get();
    }

    /**
     * 添加或更新区域信息
     *
     * <p>/xresmgr-web/region/addOrUpdate.do
     * <p>添加成功后，返回的 regionIndexCode，可用于修改区域信息，删除区域信息
     *
     * <p>/xresmgr-web/region/addOrUpdate.do
     * <p>{"type":0,"code":"0","msg":"更新成功！","data":null}
     *
     * @param catalogIndexCode    目录索引码
     * @param pRegionIndexCode    上级区域索引码。注意：更新区域信息时，这个字段似乎没有用，传递错误好像也没有影响
     * @param regionExternalCode  区域外码
     * @param pRegionExternalCode 上级区域外码。注意：更新区域信息时，这个字段似乎没有用，传递错误好像也没有影响
     * @param regionIndexCode     区域索引码。注意：添加区域信息时，此字段应为 null
     * @param regionName          区域名称
     * @param regionType          区域类型。0 - 国标区域，9 - 自定义区域
     * @param tags                标签列表。tag 之间使用 "@" 分割：
     *                            如果 tags = ["01.01", "01.01.01", "01.01.01.06"]
     *                            那么传递到海康为 "01.01@01.01.01@01.01.01.06"
     * @param tagPaths            标签路径列表。tagPath 之间使用 "," 分割：
     *                            如果 tagPaths = ["@01@01.01@", "@01@01.01@01.01.01@", "@01@01.01@01.01.01@01.01.01.06@"]
     *                            那么传递到海康为 "@01@01.01@,@01@01.01@01.01.01@,@01@01.01@01.01.01@01.01.01.06@"
     * @return 添加或更新区域信息结果
     * @throws Exception 接口请求异常
     */
    public PVIARegionVO addOrUpdate(
            String catalogIndexCode,
            String pRegionIndexCode,
            String regionExternalCode,
            String pRegionExternalCode,
            String regionIndexCode,
            String regionName,
            Integer regionType,
            Collection<String> tags,
            Collection<String> tagPaths) throws Exception {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("catalogIndexCode", catalogIndexCode);
        json.put("pRegionIndexCode", pRegionIndexCode);
        json.put("regionExternalCode", regionExternalCode);
        json.put("pRegionExternalCode", pRegionExternalCode);
        if (!Strings.isNullOrEmpty(regionIndexCode)) {
            json.put("regionIndexCode", regionIndexCode);
        }
        json.put("regionName", regionName);
        if (regionType != null) {
            json.put("regionType", regionType);
        }
        json.put("tag", String.join("@", tags));
        json.put("tagPath", String.join(",", tagPaths));

        Request request = new PostRequest.Json("/xresmgr-web/region/addOrUpdate.do", null, json);
        return hikPVIAService.doRequest(request, PVIARegionVO.class).get();
    }

    /**
     * 删除区域信息
     *
     * <p>/xresmgr-web/region/delete.do?catalogIndexCode=10&regionIndexCode=27aabb09aac443598a1b829c81a29628
     *
     * @param catalogIndexCode 目录索引码
     * @param regionIndexCode  区域索引码
     * @return 删除区域信息结果
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO deleteRegion(String catalogIndexCode, String regionIndexCode) throws Exception {
        if (Strings.isNullOrEmpty(catalogIndexCode)) {
            return error("目录索引码错误");
        }

        if (Strings.isNullOrEmpty(regionIndexCode)) {
            return error("区域索引码错误");
        }

        String url = "/xresmgr-web/region/delete.do?catalogIndexCode=" + catalogIndexCode + "&regionIndexCode=" + regionIndexCode;
        Request request = new GetRequest(url);
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 更新排序（上移、下移），内部实现为交换位置，一次调用对应一次位置交换，如果两个区域码相邻，那么表示上移、下移
     *
     * <p>/xresmgr-web/region/updateSort.do?catalogIndexCode=10&orderIndexCodes=66f86e56289e4b3f93cbd0dea4c9a9f4@7304bea4d18d4f42a1191d8e1eb8b3a3
     * <p>{"type":0,"code":"0","msg":"更新成功！","data":null}
     *
     * @param catalogIndexCode 目录索引码
     * @param regionIndexCodeA 区域索引码
     * @param regionIndexCodeB 另一个区域索引码
     * @return 更新排序结果
     * @throws Exception 接口请求异常
     */
    public PVIAObjectVO updateSort(
            String catalogIndexCode,
            String regionIndexCodeA,
            String regionIndexCodeB) throws Exception {
        if (Strings.isNullOrEmpty(catalogIndexCode)) {
            return error("目录索引码错误");
        }

        if (Strings.isNullOrEmpty(regionIndexCodeA) || Strings.isNullOrEmpty(regionIndexCodeB)) {
            return error("区域索引码错误");
        }

        String url = "/xresmgr-web/region/updateSort.do?catalogIndexCode=" + catalogIndexCode +
                "&orderIndexCodes=" + regionIndexCodeA + "@" + regionIndexCodeB;
        Request request = new GetRequest(url);
        return hikPVIAService.doRequest(request, PVIAObjectVO.class).get();
    }

    /**
     * 导出区域信息，注意：此接口将导出指定目录索引码的所有区域信息
     *
     * <p>/xresmgr-web/region/export.do?catalogIndexCode=10
     *
     * @param catalogIndexCode 目录索引码
     * @return 导出区域信息
     * @throws Exception 接口请求异常
     */
    public Object exportRegion(String catalogIndexCode) throws Exception {
        if (Strings.isNullOrEmpty(catalogIndexCode)) {
            return error("目录索引码错误");
        }

        Request request = new GetRequest("/xresmgr-web/region/export.do?catalogIndexCode=" + catalogIndexCode);
        return hikPVIAService.doRequest(request, byte[].class).get();
    }

    private PVIAObjectVO error() {
        return error("参数错误");
    }

    private PVIAObjectVO error(String err) {
        PVIAObjectVO vo = new PVIAObjectVO();
        vo.setCode("-1");
        vo.setMessage(err);
        vo.setError(err);
        return vo;
    }

}
