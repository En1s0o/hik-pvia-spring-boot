package io.en1s0o.hik.pvia.app.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.en1s0o.common.http.PostRequest;
import io.en1s0o.common.http.Request;
import io.en1s0o.hik.pvia.app.domain.vo.PVIATagListVO;
import io.en1s0o.hik.pvia.app.service.HikCatalogService;
import io.en1s0o.hik.pvia.app.service.HikManagerService;
import io.en1s0o.hik.pvia.app.service.HikRegionService;
import io.en1s0o.hik.pvia.starter.domain.vo.PVIAObjectVO;
import io.en1s0o.hik.pvia.starter.service.HikPVIAService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * HikDemoController
 *
 * @author En1s0o
 */
@Slf4j
@RestController
@RequestMapping("/api/hik")
public class HikDemoController {

    private final HikPVIAService hikPVIAService;
    private final HikManagerService hikManagerService;
    private final HikCatalogService hikCatalogService;
    private final HikRegionService hikRegionService;

    @Autowired
    public HikDemoController(HikPVIAService hikPVIAService,
                             HikManagerService hikManagerService,
                             HikCatalogService hikCatalogService,
                             HikRegionService hikRegionService) {
        this.hikPVIAService = hikPVIAService;
        this.hikManagerService = hikManagerService;
        this.hikCatalogService = hikCatalogService;
        this.hikRegionService = hikRegionService;
    }

    @GetMapping("/tagList")
    public PVIATagListVO tagList(@RequestParam(value = "type") String type) throws Exception {
        return hikManagerService.getTagList(type);
    }

    @GetMapping("/findByName")
    public PVIAObjectVO findByName(
            @RequestParam(value = "catalogIndexCode", required = false) String catalogIndexCode,
            @RequestParam(value = "regionName", required = false) String regionName) throws Exception {
        return hikRegionService.findByName(catalogIndexCode, regionName);
    }

    @GetMapping(value = "/export")
    public ResponseEntity<Object> exportRegion(
            @RequestParam(value = "catalogIndexCode") String catalogIndexCode) throws Exception {
        Object res = hikRegionService.exportRegion(catalogIndexCode);
        if (res instanceof byte[]) {
            byte[] buf = (byte[]) res;
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .contentLength(buf.length)
                    .header("Content-Disposition", "attachment; filename=catalog.csv")
                    .body(buf);
            // .body(new InputStreamResource());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/validateData")
    public PVIAObjectVO validateData() throws Exception {
        String csv = "区域外码,区域名称,上级区域外码,区域层级\n" +
                "44,广东省,10,1\n" +
                "44000000002160000003,test001,44,2\n" +
                "44000000002160000100,分公司100,44000000002160000001,3\n" +
                "44000000002160000101,分公司101,44000000002160000001,3";
        return hikCatalogService.validateData("10", csv);
    }

    // [危险接口] 导入后，需要到 PVIA 服务器删库，重启等操作！！！
    @GetMapping("/importCatalog")
    public Object importCatalog() throws Exception {
        String csv = "区域外码,区域名称,上级区域外码,区域层级\n" +
                "44,广东省,10,1\n" +
                "44000000002160000003,test001,44,2\n" +
                "44000000002160000100,分公司100,44000000002160000001,3\n" +
                "44000000002160000101,分公司101,44000000002160000001,3";
        return hikCatalogService.importCatalog("10", csv);
    }

    @GetMapping("/addOrUpdate")
    public Object addOrUpdate() throws Exception {
        return hikRegionService.addOrUpdate(
                "10",
                "xxx",
                "44000000002160000007",
                "44000000002160000001",
                "xxx",
                "华丽的名称",
                9,
                Arrays.asList("01.01", "01.01.01.06", "01.01.01"),
                Arrays.asList("@01@01.01@", "@01@01.01@01.01.01@01.01.01.06@", "@01@01.01@01.01.01@"));
    }

    @GetMapping("/upload")
    public Object upload() throws Exception {
        Collection<MultipartBody.Part> parts = new ArrayList<>();
        parts.add(MultipartBody.Part.createFormData("menuCode", "0001"));
        parts.add(MultipartBody.Part.createFormData("upload", "device.csv",
                RequestBody.create(okhttp3.MediaType.get("application/vnd.ms-excel"),
                        new File("device.csv"))));
        Request request = new PostRequest.Multipart(
                "/xresmgr-web/device/validateData.do",
                null, parts);
        return hikPVIAService.doRequest(request, ObjectNode.class).get();
    }

}
