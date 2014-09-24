package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

@Controller
public class MySpringController {

	private VideoFileManager videoFileManager;

	private static final AtomicLong currentId = new AtomicLong(0L);

	private final Map<Long, Video> videoIdMap = new HashMap<Long, Video>();

	private List<Video> videos = new ArrayList<Video>();

	public MySpringController() throws IOException {
		videoFileManager = VideoFileManager.get();
	}

	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody List<Video> getVideoList() {
		return videos;
	}

	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
	public void getVideoData(@PathVariable("id") long id, HttpServletResponse response) throws IOException {
		if (id == 0 || videoIdMap.get(id) == null) {
			response.sendError(404, "Invalid Video ID.");
			return;
		}
		Video v = videoIdMap.get(id);
		try {
			videoFileManager.copyVideoData(v, response.getOutputStream());
		} catch (IOException e) {
			response.sendError(400, "Error loading video.");
		}
	}

	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video postVideo(@RequestBody Video v) {
		save(v);
		videos.add(v);
		return v;
	}

	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
	public @ResponseBody VideoStatus postVideoData(@PathVariable("id") long id,
			@RequestParam("data") MultipartFile videoData) throws IOException, NoSuchRequestHandlingMethodException {
		VideoStatus status = new VideoStatus(VideoState.PROCESSING);
		if (id == 0 || videoIdMap.get(id) == null) {
			throw new NoSuchRequestHandlingMethodException("postVideoData", MySpringController.class);
		}

		Video v = videoIdMap.get(id);
		videoFileManager.saveVideoData(v, videoData.getInputStream());
		status.setState(VideoState.READY);
		return status;
	}

	public Video save(Video entity) {
		// Check to see if the entity has an ID.
		if (entity.getId() == 0) {
			entity.setId(currentId.incrementAndGet());
		}

		// Set the URL to access that video.
		entity.setDataUrl(getUrlBaseForLocalServer() + "/video/" + entity.getId() + "/data");

		// Save the entity in memory.
		videoIdMap.put(entity.getId(), entity);
		return entity;
	}

	private String getUrlBaseForLocalServer() {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String base = "http://" + req.getServerName() + ((req.getServerPort() != 80) ? ":" + req.getServerPort() : "");
		return base;
	}

}
