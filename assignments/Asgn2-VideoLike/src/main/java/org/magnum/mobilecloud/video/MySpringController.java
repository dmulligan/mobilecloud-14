package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MySpringController /* implements VideoSvcApi */{

	// Guide: https://class.coursera.org/mobilecloud-001/forum/thread?thread_id=924

	@Autowired
	private VideoRepository videoRepository;

	// @GET(VIDEO_SVC_PATH)
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return (Collection<Video>) videoRepository.findAll();
	}

	// @GET(VIDEO_SVC_PATH + "/{id}")
	// public Video getVideoById(@Path("id") long id);
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id, HttpServletResponse response) {
		Video v = null;
		try {
			v = videoRepository.findOne(id);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (v == null) {
				response.setStatus(HttpStatus.SC_NOT_FOUND);
			}
		}
		return v;
	}

	// @POST(VIDEO_SVC_PATH)
	// public Video addVideo(@Body Video v);
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		v.setLikes(0);
		return videoRepository.save(v);
	}

	// @POST(VIDEO_SVC_PATH + "/{id}/like")
	// public Void likeVideo(@Path("id") long id);
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public void likeVideo(@PathVariable("id") long id, Principal principal, HttpServletResponse response) {
		Video v = videoRepository.findOne(id);
		if (v == null) {
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return;
		}
		String username = principal.getName();
		if (!v.like(username)) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		}
		videoRepository.save(v);
	}

	// @POST(VIDEO_SVC_PATH + "/{id}/unlike")
	// public Void unlikeVideo(@Path("id") long id);
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public void unlikeVideo(@PathVariable("id") long id, Principal principal, HttpServletResponse response) {
		Video v = videoRepository.findOne(id);
		if (v == null) {
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return;
		}
		String username = principal.getName();
		v.unlike(username);
		videoRepository.save(v);
	}

	// @GET(VIDEO_TITLE_SEARCH_PATH)
	// public Collection<Video> findByTitle(@Query(TITLE_PARAMETER) String title);
	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String name) {
		return videoRepository.findByName(name);
	}

	// @GET(VIDEO_DURATION_SEARCH_PATH)
	// public Collection<Video> findByDurationLessThan(@Query(DURATION_PARAMETER) long duration);
	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		return videoRepository.findByDurationLessThan(duration);
	}

	// @GET(VIDEO_SVC_PATH + "/{id}/likedby")
	// public Collection<String> getUsersWhoLikedVideo(@Path("id") long id);
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id,
			HttpServletResponse response) {
		Video v = videoRepository.findOne(id);
		if (v == null) {
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return new ArrayList<String>();
		}
		return v.getLikeUsers();
	}

}
