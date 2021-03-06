package com.GhostlyRunes;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static java.lang.System.in;

/**
 * Created by Charlie on 25/01/2017.
 */

public class TouchHandler implements View.OnTouchListener {
    View p1,p2; //Image of points p1 and p2 of our minimgame. Its names should be Point1 and Point2 if default is used.



    float lkx,lky; //to check previous movement, last known x,y
    float ox,oy; //origin coordinates (p1)
    float dx,dy; //destiny coordinates (p2)
    public float error=100f; //error on fingertips
    public float alpha=0;//0.5f; //alpha in [0,1], checks optimality of the descent (1: optimality)
    boolean path=false; //If the events are a possible solution to the minigame
    boolean checking=false; //enable minigame


    int[] pattern; //Vector of tags of elements to be done in order
    int pattern_done=0; //Number of lines of the pattern done
    private boolean pattern_new=true; //True if the pattern is new and needs to be processed
    private View last_view; //Maybe unnecesary

    MessageReceiver MR; //This will get messages from here. Usually an activity to show results to user.
    String TOUCHID;

    //DEBUGGING OPTIONS
    boolean draw=false; //If true, draws a point in the position of your finger each time an event triggers. Debugging purposes.
    boolean debug=true; //enable debugging output

    public TouchHandler(MessageReceiver MR, String TOUCHID){
        this.MR=MR;
        this.TOUCHID=TOUCHID;
    }

    public TouchHandler(MessageReceiver MR, String TOUCHID, int[] points_id){
        this.MR=MR;
        this.TOUCHID=TOUCHID;
        setPattern(points_id);
    }

    @Override
    public boolean onTouch (View v, MotionEvent event){

        if(checking) {
            float x = event.getX(), y = event.getY();

            if (pattern_new) { //Gets the points to a correct state the first time it triggers
                resetPattern(v);
                if(debug) Log.d("Points", "Origin: " + ox + " " + oy);
                if(debug) Log.d("Points", "Destiny: " + dx + " " + dy);
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getPointerCount() == 1) {
                    lkx = x;
                    lky = y;
                }
                if (distance(x, y, ox, oy) < error) {
                    path = true;
                    if(debug) Log.d("PRESS_EVENT", "P1 pressed");

                        MR.transmitMessage(TOUCHID, "pathStart");

                } else {
                    path = false;
                    if (distance(x, y, dx, dy) < error) {
                        if(debug) Log.d("PRESS_EVENT", "P2 pulsado");
                    }
                }
                if(debug) Log.d("PRESS_EVENT", "" + event.getX() + " " + event.getY());
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (distance(x, y, ox, oy) < error) {
                    if(!path){

                        MR.transmitMessage(TOUCHID, "pathStart");

                    }
                    path = true;
                    if(debug) Log.d("PATH_EVENT", "P1 pulsado");


                }else{
                if (path) {
                    if (debug)
                        Log.d("MOVE_EVENT", "Position: " + event.getX() + " " + event.getY() + ". Distance: " + distance(x, y, dx, dy));
                    if (distance(x, y, dx, dy) < error) {
                        if (debug) Log.d("PATH_EVENT", "P2 reached");
                        pattern_done++;
                        if (pattern == null || pattern_done >= pattern.length - 1) {
                            if (debug) Log.d("END OF PATTERN", "" + pattern_done);
                            path = false;

                            resetPattern(v);
                            //pattern_new=true;
                            MR.transmitMessage(TOUCHID, "destinyReached");

                        } else {
                            if (debug) Log.d("MID POINT REACHED", "DONE:" + pattern_done);
                            //continuePattern(v);
                            //p1=p2;

                            //DEBUG
                            float pox = ox;
                            float poy = oy;
                            float pdx = dx;
                            float pdy = dy;
                            //!DEBUG

                            p1 = v.findViewById(pattern[pattern_done]);
                            p2 = v.findViewById(pattern[pattern_done + 1]);
                            ox = p1.getRight();
                            oy = p1.getTop();
                            dx = p2.getRight();
                            dy = p2.getTop();

                            if (debug)
                                Log.d("CONTINUE PATTERN", "DONE: " + pattern_done + " NEW O: " + ox + " " + oy + ".");
                            if (debug)
                                Log.d("CONTINUE PATTERN", "DONE: " + pattern_done + " OLD O: " + pox + " " + poy + ".");
                            if (debug)
                                Log.d("CONTINUE PATTERN", "DONE: " + pattern_done + " NEW D: " + dx + " " + dy + ".");
                            if (debug)
                                Log.d("CONTINUE PATTERN", "DONE: " + pattern_done + " OLD D: " + pdx + " " + pdy + ".");


                            MR.transmitMessage(TOUCHID, "partialPatternDone");
                        }
                    } else if((distance(x,y,ox,oy)+distance(x,y,dx,dy))>error+distance(ox,oy,dx,dy)){ //If not within the ellipse with origin and destiny as focus and constant error+distance
                            path = false;


                            if (debug)
                                Log.d("PATH_EVENT", "End of path: Distance A:" + distance(x, y, dx, dy) + ", LK:" + distance(lkx, lky, dx, dy) + ", DIFF:" + (distance(lkx, lky, dx, dy) - distance(x, y, dx, dy)) + ",TOL:" + distance(lkx, lky, x, y) * alpha);
                            if (debug)
                                Log.d("PATH_EVENT", "Points were: O:" + ox + " " + oy + ", D:" + dx + " " + dy + ", P:" + x + " " + y);
                            if (debug)
                                Log.d("PATH EVENT", "Distances: O:" + distance(x, y, ox, ox) + ", D:" + distance(x, y, dx, dy));

                            MR.transmitMessage(TOUCHID, "pathLost");

                            if (pattern_done != 0) {
                                resetPattern(v);
                                // pattern_new =true;
                            }


                    }
                }

                    lkx = x;
                    lky = y;

                }


            }
            return true; //true to be able to capture move events after a down event
        }else{
            if(debug) Log.d("TOUCH LISTENER WARNING","TOUCHED BUT STATUS: DISCONNECTED");
            return false;
        }

    }

    private float distance(float x1,float y1, float x2, float y2){
        return (float) Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    }

    public void setError(float e){
        this.error=e;
    }

    public void startChecking(){
        checking=true;
        pattern_new=true;
    }

    public void stopChecking(){
        checking=false;
        path=false;
    }

    public void setPattern( int[] points_id){
        if(points_id.length>=2) this.pattern=points_id;
        else {
            pattern=null;
            Log.w("TOUCH HANDLER","Pattern without enough points, will set to default");
        }
        pattern_new=true;

        //strict debug, crash if null
        if(debug) Log.d("NEW PATTERN","LENGTH: "+points_id.length);
    }

    private void resetPattern(View v){
        if(pattern==null) {
            p1 = v.findViewById(R.id.Point1);
            p2 = v.findViewById(R.id.Point2);

        }else {
            p1 = v.findViewById(pattern[0]);
            p2 = v.findViewById(pattern[1]);
        }
        ox = p1.getRight();
        oy = p1.getTop();
        dx = p2.getRight();
        dy = p2.getTop();
        pattern_new=false;
        if(debug) Log.d("PATTERN RESET","DONE: "+pattern_done);
        pattern_done=0;
    if(pattern!=null){
        if(debug) Log.d("PATTERN RESET", "PATTERN: ");

        for(int i : pattern){
            if(debug) Log.d("PATTERN RESET", " "+ v.findViewById(i).getRight()+" "+ v.findViewById(i).getTop());

        }
    }
    }
    private void continuePattern(View v){
        //p1=p2;

        //DEBUG
        float pox=ox;
        float poy=oy;
        float pdx=dx;
        float pdy=dy;
        //!DEBUG

        p1=v.findViewById(pattern[pattern_done]);
        p2=v.findViewById(pattern[pattern_done+1]);
        ox = p1.getRight();
        oy = p1.getTop();
        dx = p2.getRight();
        dy = p2.getTop();

        if(debug) Log.d("CONTINUE PATTERN","DONE: "+pattern_done +" NEW O: "+ox + " "+oy +".");
        if(debug) Log.d("CONTINUE PATTERN","DONE: "+pattern_done +" OLD O: "+pox + " "+poy +".");
        if(debug) Log.d("CONTINUE PATTERN","DONE: "+pattern_done +" NEW D: "+dx + " "+dy +".");
        if(debug) Log.d("CONTINUE PATTERN","DONE: "+pattern_done +" OLD D: "+pdx + " "+pdy +".");

    }

}
