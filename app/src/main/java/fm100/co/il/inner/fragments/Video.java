package fm100.co.il.inner.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.adapters.ChannelListAdapter;
import fm100.co.il.adapters.StationListAdapter;
import fm100.co.il.adapters.VideoLvAdapter;
import fm100.co.il.busEvents.NotificationBusEvent;
import fm100.co.il.busEvents.StationBusEvent;
import fm100.co.il.busEvents.VideoListBusEvent;
import fm100.co.il.models.RunShape;
import fm100.co.il.models.VideoObj;

/************************************************
 * The Video Fragment Class , should create the Video Tab Activity and show Streams of video
 ************************************************/

public class Video extends Fragment {

	private ImageButton startStreamBtn;
	private TextView songNameTv;
	private WebView webView;
	private ListView videoLv;
	private List<VideoObj> videoList = new ArrayList<>();
	private VideoLvAdapter videoLvAdapter;

	VideoView videoView;
	String vidAddress = "http://hlscdn.streamgates.net/radios100fm/abr/playlist.m3u8";//"http://100fm.multix.co.il/";
	//String vidAddress = "rtmp:\\/\\/37.49.225.94:1935\\/radios100fm";
	// another adress = "http://100fm.multix.co.il/"
	// another testing adress = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"
	Uri vidUri = Uri.parse(vidAddress);

	private int listCreated = 0;

	String mainVideoLink = "https://www.youtube.com/embed/";

	public static final int USER_MOBILE = 0;
	public static final int USER_DESKTOP = 1;

	private ProgressBar videoLvProgress;

	private VideoObj firstVideoObj;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.video_fragment, container, false);
		EventBus.getDefault().register(this);
		//ImageButton startStreamBtn = (ImageButton) v.findViewById(R.id.startStreamBtn);
		webView = (WebView) v.findViewById(R.id.webView);
		videoLv = (ListView) v.findViewById(R.id.videoLv);
		//VideoView vidView = (VideoView)v.findViewById(R.id.videoView);
		videoLvProgress = (ProgressBar) v.findViewById(R.id.videoLvProgress);
		//task.execute("JsonCo.json");

		//webView.setVisibility(View.INVISIBLE);

		/*Intent i = new Intent(android.content.Intent.ACTION_VIEW , vidUri);
		i.setDataAndType(vidUri , "video/*");
		startActivity(i);
		*/

		/*String url ="http://100fm.multix.co.il/";
		WebView webView = (WebView) v.findViewById(R.id.webView);
		webView.setWebViewClient(new WebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);
		*/

		/*firstVideoObj = new VideoObj();
		firstVideoObj.setTitle(MainActivity.getMyApplicationContext().getString(R.string.firstVideoTitle));
		firstVideoObj.setThumbnail("http://a5.mzstatic.com/us/r30/Purple69/v4/3d/b8/29/3db829f9-7432-1707-7945-cdcfb3cd5cc1/icon175x175.jpeg");
		firstVideoObj.setPublished("live");*/

		if (listCreated == 0 && videoList!=null){
			videoLvAdapter = new VideoLvAdapter(getActivity() , videoList);
			videoLv.setAdapter(videoLvAdapter);
		}

		//changeVideoUrl(vidAddress);


		webView.loadData("<html><body style=\"margin: 0;\"><video width=\"100%\" height=\"100%\" preload=\"none\" poster=\"http://assets-jpcust.jwpsrv.com/thumbs/teD8sDdM-720.jpg\"><source type=\"application/x-mpegURL\" src=\"http://hlscdn.streamgates.net/radios100fm/abr/playlist.m3u8\" /></video></body></html>", "text/html; charset=UTF-8", null);

		return v;
	}

	public void setStationData(JSONObject obj) throws JSONException {
		JSONObject parentObject2 = obj.getJSONObject("video");

		JSONArray parentArray = parentObject2.getJSONArray("archive");

		VideoObj tempVideo = null;

		tempVideo = new VideoObj();
		tempVideo.setId("live");
		tempVideo.setPublished("");
		tempVideo.setThumbnail("http://assets-jpcust.jwpsrv.com/thumbs/teD8sDdM-720.jpg");
		tempVideo.setTitle("רדיוס 100FM לייב");
		videoList.add(tempVideo);

		for (int i = 0; i < parentArray.length(); i++) {

			tempVideo = new VideoObj();
			JSONObject finalObject = parentArray.getJSONObject(i);
			tempVideo.setId((finalObject.getString("id")));
			tempVideo.setPublished(finalObject.getString("published"));
			tempVideo.setThumbnail(finalObject.getString("thumbnail"));
			tempVideo.setTitle(finalObject.getString("title"));

			videoList.add(tempVideo);
		}

		videoLvProgress.setVisibility(View.GONE);
		videoLvAdapter = new VideoLvAdapter(getActivity() , videoList);
		videoLv.setAdapter(videoLvAdapter);
		videoLv.setOnItemClickListener(onVideoClick);
	}

	public void changeVideoUrl(String url) {
		webView.setWebViewClient(new WebViewClient());
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		//webView.loadData(url, "text/html", "utf-8");
		webView.loadUrl(url);
	}

	// catching list of stations from main activity and attaching to list
	@Subscribe
	public void VideoListBusEvent(VideoListBusEvent videoEvent) {
		videoList = videoEvent.getVideoListBusMsg();
		videoLvAdapter = new VideoLvAdapter(getActivity() , videoList);
		videoLv.setAdapter(videoLvAdapter);
		videoLv.setOnItemClickListener(onVideoClick);
		listCreated =1;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	AdapterView.OnItemClickListener onVideoClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String videoId =videoList.get(position).getId();
			String itemURL = mainVideoLink+videoId;

			if( videoId.equals("live") ) {
				webView.loadUrl("");
				webView.loadData("<html><body style=\"margin: 0;\"><video width=\"100%\" height=\"100%\" preload=\"none\" poster=\"http://assets-jpcust.jwpsrv.com/thumbs/teD8sDdM-720.jpg\"><source type=\"application/x-mpegURL\" src=\"http://hlscdn.streamgates.net/radios100fm/abr/playlist.m3u8\" /></video></body></html>", "text/html; charset=UTF-8", null);
				webView.reload();
			} else {
				webView.loadData("", "text/html; charset=UTF-8", null);
				changeVideoUrl(itemURL);
			}
		}
	};
}

