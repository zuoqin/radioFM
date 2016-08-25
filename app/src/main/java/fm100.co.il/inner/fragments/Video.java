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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
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

	String vidAddress = "http://100fm.multix.co.il/";
	//String vidAddress = "rtmp:\\/\\/37.49.225.94:1935\\/radios100fm";
	// another adress = "http://100fm.multix.co.il/"
	// another testing adress = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"
	Uri vidUri = Uri.parse(vidAddress);

	private int listCreated = 0;

	String mainVideoLink = "https://www.youtube.com/embed/";

	public static final int USER_MOBILE = 0;
	public static final int USER_DESKTOP = 1;

	ReadFileTask task = new ReadFileTask();
	videoFromJsonTask vidTask = new videoFromJsonTask();

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.video_fragment, container, false);
		EventBus.getDefault().register(this);
		ImageButton startStreamBtn = (ImageButton) v.findViewById(R.id.startStreamBtn);
		webView = (WebView) v.findViewById(R.id.webView);
		videoLv = (ListView) v.findViewById(R.id.videoLv);
		//VideoView vidView = (VideoView)v.findViewById(R.id.videoView);

		webView.setVisibility(View.INVISIBLE);

		task.execute("JsonCo.json");

		startStreamBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "http://100fm.multix.co.il/";
				webView.setWebChromeClient(new WebChromeClient());
				WebSettings webSettings = webView.getSettings();
				webSettings.setJavaScriptEnabled(true);
				webSettings.setUseWideViewPort(true);
				webSettings.setLoadWithOverviewMode(true);
				webView.loadUrl(url);
			}


		/*vidView.setVideoURI(vidUri);
		MediaController vidControl = new MediaController(getActivity());
		vidControl.setAnchorView(vidView);
		vidView.setMediaController(vidControl);
		vidView.requestFocus();
		vidView.start();
		*/

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
		});

		if (listCreated == 0 && videoList!=null){
			videoLvAdapter = new VideoLvAdapter(getActivity() , videoList);
			videoLv.setAdapter(videoLvAdapter);
		}


		return v;
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

			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(itemURL)));
		}
	};

	// JSON RELATED ASYNCTASKS
	public class ReadFileTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String temp="";
			try {
				FileInputStream fin = MainActivity.getMyApplicationContext().openFileInput(params[0]);
				int c;
				while( (c = fin.read()) != -1){
					temp = temp + Character.toString((char)c);
				}
				fin.close();
			} catch (Exception e){
				Log.e("MyLog" , "read task exception: " + e.getMessage());
			}
			return temp;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//videoList = videosFromJson(result);
			vidTask.execute(result);
			//videoLvAdapter = new VideoLvAdapter(getActivity() , videoList);
			//videoLv.setAdapter(videoLvAdapter);
			//videoLv.setOnItemClickListener(onVideoClick);
			listCreated =1;
		}
	}

	public class videoFromJsonTask extends AsyncTask<String, Void, List<VideoObj>> {

		@Override
		protected List<VideoObj> doInBackground(String... params) {
			try {
				JSONObject parentObject = new JSONObject(params[0]);

				JSONObject parentObject2 = parentObject.getJSONObject("video");

				JSONArray parentArray = parentObject2.getJSONArray("archive");

				VideoObj tempVideo = null;

				for (int i = 0; i < parentArray.length(); i++) {

					tempVideo = new VideoObj();
					JSONObject finalObject = parentArray.getJSONObject(i);
					tempVideo.setId((finalObject.getString("id")));
					tempVideo.setPublished(finalObject.getString("published"));
					tempVideo.setThumbnail(finalObject.getString("thumbnail"));
					tempVideo.setTitle(finalObject.getString("title"));

					videoList.add(tempVideo);
				}
			} catch (Exception e) {
				Log.e("HereLog", "Json Exception" + e.getMessage());
			}

			return videoList;
		}

		@Override
		protected void onPostExecute(List<VideoObj> videoObjs) {
			super.onPostExecute(videoObjs);
			videoList = videoObjs;
			videoLvAdapter = new VideoLvAdapter(getActivity() , videoList);
			videoLv.setAdapter(videoLvAdapter);
			videoLv.setOnItemClickListener(onVideoClick);
		}
	}
}

