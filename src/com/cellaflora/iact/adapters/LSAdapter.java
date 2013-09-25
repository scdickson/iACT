package com.cellaflora.iact.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cellaflora.iact.Constants;
import com.cellaflora.iact.FullScreenImageView;
import com.cellaflora.iact.MainActivity;
import com.cellaflora.iact.objects.Post;
import com.cellaflora.iact.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sdickson on 7/21/13.
 */
public class LSAdapter extends BaseAdapter
{
    public static int IMAGE_SIZE;

    Context context;
    ArrayList<Post> LSItems;
    LayoutInflater inflater;

    public LSAdapter(Context context, ArrayList<Post> LSItems)
    {
        this.context = context;
        this.LSItems = LSItems;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        IMAGE_SIZE = display.getWidth() / 3;
    }

    public int getCount()
    {
        return LSItems.size();
    }

    public Object getItem(int position)
    {
        return LSItems.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.ls_list_item, parent, false);
        ImageView ls_image = (ImageView) itemView.findViewById(R.id.ls_image);
        ImageView ls_alert = (ImageView) itemView.findViewById(R.id.ls_alert);
        TextView ls_headline = (TextView) itemView.findViewById(R.id.ls_headline);
        ls_headline.setTypeface(MainActivity.Futura);
        TextView ls_desc = (TextView) itemView.findViewById(R.id.ls_desc);
        ls_desc.setTypeface(MainActivity.Futura);
        TextView ls_date = (TextView) itemView.findViewById(R.id.ls_date);
        ls_date.setTypeface(MainActivity.Futura);

        Post tmp = LSItems.get(position);

        if(tmp.isAlert)
        {
            ls_alert.setVisibility(View.VISIBLE);
        }
        else
        {
            ls_alert.setVisibility(View.GONE);
        }

        if(tmp.dateline != null && !tmp.dateline.isEmpty())
        {
            ls_date.setText("- " + tmp.dateline + " -");
        }
        else
        {
            ls_date.setVisibility(View.GONE);
        }

        if(tmp.headline != null && !tmp.headline.isEmpty())
        {
            ls_headline.setText(tmp.headline);
        }
        else
        {
            ls_headline.setVisibility(View.GONE);
        }

        if(tmp.description != null && !tmp.description.isEmpty())
        {
            ls_desc.setText(tmp.description);
        }
        else
        {
            ls_desc.setVisibility(View.GONE);
        }

        if(tmp.photo_url != null)
        {
            File f = new File(context.getFilesDir() + "/" + tmp.objectId);
            if(f != null && f.exists())
            {
                try
                {
                    ls_image.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(f)));
                }
                catch(Exception ex)
                {
                    new loadImageFromParse().execute(ls_image, tmp);
                }
            }
            else
            {
                new loadImageFromParse().execute(ls_image, tmp);
            }

        }
        else
        {
            ls_image.setVisibility(View.GONE);
        }

        return itemView;
    }

    private class loadImageFromParse extends AsyncTask<Object, Integer, Void>
    {
        ImageView photo;
        Bitmap image;
        File compressed, file;
        Post post;

        protected Void doInBackground(Object... arg0)
        {
            try
            {
                photo = (ImageView) arg0[0];
                post = (Post) arg0[1];
                URL url = new URL(post.photo_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream is = connection.getInputStream();

                file = new File(context.getFilesDir() + "/" + post.objectId + "_uncompressed");
                compressed = new File(context.getFilesDir() + "/" + post.objectId);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, Constants.IMAGE_BUFFER_SIZE);
                byte data[] = new byte[Constants.IMAGE_BUFFER_SIZE];

                int bytesRead = 0;
                while((bytesRead = is.read(data, 0, data.length)) >= 0)
                {
                    bos.write(data, 0, bytesRead);
                }

                bos.close();
                fos.close();
                is.close();

                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(file),null,o);

                int width_tmp=o.outWidth, height_tmp=o.outHeight;
                int scale=1;
                while(true){
                    if(width_tmp/2<IMAGE_SIZE || height_tmp/2<IMAGE_SIZE)
                        break;
                    width_tmp/=2;
                    height_tmp/=2;
                    scale*=2;
                }

                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize=scale;
                image = BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
                FileOutputStream out = new FileOutputStream(compressed);
                image.compress(Bitmap.CompressFormat.JPEG, 90, out);

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            if(image != null)
            {
                final Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(1000);
                photo.setImageBitmap(image);
                photo.startAnimation(in);

                photo.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent fullscreenimage = new Intent(context, FullScreenImageView.class);
                        fullscreenimage.putExtra("image", file.getAbsolutePath() + "_uncompressed");
                        fullscreenimage.putExtra("caption", post.caption);
                        context.startActivity(fullscreenimage);
                    }
                });
            }
        }
    }

    private class loadImage extends AsyncTask<Object, Integer, Void>
    {
        ImageView photo;
        Bitmap image;
        File f;
        Post post;

        protected Void doInBackground(Object... arg0)
        {
            try
            {
                photo = (ImageView) arg0[0];
                f = (File) arg0[1];
                post = (Post) arg0[2];
                image = BitmapFactory.decodeStream(new FileInputStream(f));
            }
            catch(OutOfMemoryError ome)
            {
                try
                {
                    photo = (ImageView) arg0[0];
                    f = (File) arg0[1];

                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(new FileInputStream(f),null,o);

                    int width_tmp=o.outWidth, height_tmp=o.outHeight;
                    int scale=1;
                    while(true){
                        if(width_tmp/2<IMAGE_SIZE || height_tmp/2<IMAGE_SIZE)
                            break;
                        width_tmp/=2;
                        height_tmp/=2;
                        scale*=2;
                    }

                    BitmapFactory.Options o2 = new BitmapFactory.Options();
                    o2.inSampleSize=scale;
                    image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
                }
                catch(Exception ex){}
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            if(image != null)
            {
                final Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(150);
                photo.setImageBitmap(image);
                photo.startAnimation(in);
                photo.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent fullscreenimage = new Intent(context, FullScreenImageView.class);
                        fullscreenimage.putExtra("image", f.getAbsolutePath() + "_uncompressed");
                        fullscreenimage.putExtra("caption", post.caption);
                        context.startActivity(fullscreenimage);
                    }
                });
            }
        }
    }


}
