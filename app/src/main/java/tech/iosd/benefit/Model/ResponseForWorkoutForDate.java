package tech.iosd.benefit.Model;

/**
 * Created by SAM33R on 28-06-2018.
 */

public class ResponseForWorkoutForDate {
    private boolean success;
    public String message;
    private Data data;

    public ResponseForWorkoutForDate(boolean success,  Data data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public  Data getData() {
        return data;
    }

    public void setData( Data data) {
        this.data = data;
    }

    public class Data{
        private String _id;
        private String client;
        private Workout workout;
        private int videoCount;
        public Data(String _id, String client, Workout workout, int videoCount) {
            this._id = _id;
            this.client = client;
            this.workout = workout;
            this.videoCount = videoCount;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public Workout getWorkout() {
            return workout;
        }

        public void setWorkout(Workout workout) {
            this.workout = workout;
        }

        public int getVideoCount() {
            return videoCount;
        }

        public void setVideoCount(int videoCount) {
            this.videoCount = videoCount;
        }
    }

}
