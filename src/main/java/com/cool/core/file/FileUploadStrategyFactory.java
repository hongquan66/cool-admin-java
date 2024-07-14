package com.cool.core.file;

import static com.cool.core.plugin.consts.PluginConsts.uploadHook;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjUtil;
import com.cool.core.exception.CoolPreconditions;
import com.cool.core.file.strategy.FileUploadStrategy;
import com.cool.core.plugin.service.CoolPluginService;
import com.cool.modules.plugin.entity.PluginInfoEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadStrategyFactory {

	final private ApplicationContext applicationContext;

	final private CoolPluginService coolPluginService;

	public FileUploadStrategy getStrategy() {
		PluginInfoEntity pluginInfoEntity = coolPluginService.getPluginInfoEntityByHook(uploadHook);
		return getStrategy(pluginInfoEntity);
	}

	private FileUploadStrategy getStrategy(PluginInfoEntity pluginInfoEntity) {
		if (ObjUtil.isEmpty(pluginInfoEntity)) {
			return applicationContext.getBean("localFileUploadStrategy", FileUploadStrategy.class);
		}
		return applicationContext.getBean("ossFileUploadStrategy", FileUploadStrategy.class);
	}

	public Object upload(MultipartFile[] files, HttpServletRequest request) {
		PluginInfoEntity pluginInfoEntity = coolPluginService.getPluginInfoEntityByHook(uploadHook);
		try {
			return getStrategy(pluginInfoEntity).upload(files, request, pluginInfoEntity);
		} catch (IOException e) {
			log.error("上传文件失败", e);
			CoolPreconditions.alwaysThrow("上传文件失败 {}", e.getMessage());
		}
		return null;
	}

	public Object getMode() {
		FileUploadStrategy strategy = getStrategy();
		UpLoadModeType upLoadModeType = strategy.getMode();
		return Dict.create().set("mode", upLoadModeType.getMode().value())
			.set("type", upLoadModeType.getType());
	}
}