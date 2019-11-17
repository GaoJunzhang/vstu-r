package com.zgj.mps.controller.vstu;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zgj.mps.bean.DownStatusBean;
import com.zgj.mps.bean.DownloadBean;
import com.zgj.mps.model.Download;
import com.zgj.mps.model.Resource;
import com.zgj.mps.model.User;
import com.zgj.mps.model.UserResource;
import com.zgj.mps.service.IDownloadService;
import com.zgj.mps.service.IResourceService;
import com.zgj.mps.service.IUserResourceService;
import com.zgj.mps.tool.ResultUtil;
import com.zgj.mps.tool.ShiroSecurityUtil;
import com.zgj.mps.vo.PageVo;
import com.zgj.mps.vo.Result;
import com.zgj.mps.vo.SearchVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(description = "下载资源管理接口")
@RequestMapping("/download")
@Transactional
public class DownloadController {

    @Autowired
    private IDownloadService iDownloadService;

    @Autowired
    private ShiroSecurityUtil shiroSecurityUtil;

    @Autowired
    private IUserResourceService iUserResourceService;

    @Autowired
    private IResourceService iResourceService;

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "通过id获取")
    public Result<Download> get(@PathVariable String id) {

        Download download = iDownloadService.getById(id);
        return new ResultUtil<Download>().setData(download);
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    @ApiOperation(value = "获取全部数据")
    public Result<List<Download>> getAll() {

        List<Download> list = iDownloadService.list();
        return new ResultUtil<List<Download>>().setData(list);
    }

    @RequestMapping(value = "/getByPage", method = RequestMethod.GET)
    @ApiOperation(value = "分页获取")
    public Object getByPage(@ModelAttribute PageVo pageVo,
                                                @ModelAttribute SearchVo searchVo,
                                                @ModelAttribute DownloadBean downloadBean) {

        Page<DownloadBean> downloadBeanPage = iDownloadService.pageDownload(downloadBean, pageVo, searchVo);
        Map<String, Object> map = new HashMap<>();
        map.put("data", downloadBeanPage.getRecords());
        map.put("pageNo", downloadBeanPage.getPages());
        map.put("totalCount", downloadBeanPage.getTotal());
        return new ResultUtil<>().setData(map);
    }

    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    @ApiOperation(value = "编辑或更新数据")
    public Result<Download> saveOrUpdate(@ModelAttribute Download download) {

        if (download.getCreateTime() == null) {
            download.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        if (download.getStatus() == 1){
            download.setFinishTime(new Timestamp(System.currentTimeMillis()));
        }
        if (iDownloadService.saveOrUpdate(download)) {
            return new ResultUtil<Download>().setSuccessMsg("成功");
        }
        return new ResultUtil<Download>().setErrorMsg("操作失败");
    }

    @RequestMapping(value = "/delByIds/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量通过id删除")
    public Result<Object> delAllByIds(@PathVariable String[] ids) {

        for (String id : ids) {
            iDownloadService.removeById(id);
        }
        return new ResultUtil<Object>().setSuccessMsg("批量通过id删除数据成功");
    }

    @RequestMapping(value = "/downProcessByDevictType", method = RequestMethod.GET)
    @ApiOperation(value = "查询下载数据")
    public Result<List<DownStatusBean>> downProcessByDevictType(@RequestParam(value = "type",required = true) String type){
        List<DownStatusBean> list = iDownloadService.downProcessByDevictType(type);
        return new ResultUtil<List<DownStatusBean>>().setData(list);
    }

    @PostMapping(value = "/addDownLoad")
    @ApiOperation(value = "修改用户要下载的资源列表")
    public Object addDownLoad(@RequestParam(name = "deviceId", required = true) String deviceId,
                              String rids){
        JSONArray jsonArray = JSONUtil.parseArray(rids);
        User user = shiroSecurityUtil.getCurrentUser();
        if (jsonArray.size()<=0){
            return new ResultUtil<>().setErrorMsg("资源list不可用");
        }
        List<Download> downloads = new ArrayList<>();
        List<Map<String,String>> list = new ArrayList<>();
        for (int i=0;i<jsonArray.size();i++){
            JSONObject job = jsonArray.getJSONObject(i);
            String rid = job.get("rid")+"";
            if (StrUtil.isEmpty(rid)){
                continue;
            }
            Map<String,String> map = new HashMap<>();
            List<UserResource> userResources = iUserResourceService.findByUserIdAndResourceId(user.getId(),rid);
            map.put("rid", rid);

            if (userResources.size()<=0){
                map.put("msg","资源无效");
                list.add(map);
                continue;
            }
            if (userResources.get(0).getIsForever() == 1) {
                Timestamp nowTime = new Timestamp(System.currentTimeMillis());
                if (nowTime.before(userResources.get(0).getStartTime()) || nowTime.after(userResources.get(0).getEndTime())) {
                    map.put("msg","资源不在有效期，有效期为：【" + userResources.get(0).getStartTime() + "-" + userResources.get(0).getEndTime() + "】");
                    list.add(map);
                    continue;
                }
            }
            Resource resource = iResourceService.getById(rid);
            map.put("resourceName", resource.getName());
            map.put("url", resource.getUrl());
            Download download = new Download();
            download.setResourceId(job.get("id")+"");
            download.setIsDelete((short)0);
            download.setCreateTime(new Timestamp(System.currentTimeMillis()));
            download.setUserId(user.getId());
            download.setStatus(Long.parseLong(job.get("status")+""));
            download.setDeviceId(deviceId);
            downloads.add(download);
            list.add(map);
        }
        if (iDownloadService.saveBatch(downloads)){
            return new ResultUtil<>().setData(list);
        }
        return new ResultUtil<>().setErrorMsg("请求失败");
    }

    @GetMapping(value = "/getDownLoads")
    @ApiOperation(value = "获取用户要下载的资源")
    public Object getDownLoads(@RequestParam(name = "deviceId", required = true) String deviceId){
        User user = shiroSecurityUtil.getCurrentUser();
        return new ResultUtil<>().setData(iDownloadService.userDownList(user.getId(),deviceId));
    }
}
