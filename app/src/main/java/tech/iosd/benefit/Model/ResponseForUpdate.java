package tech.iosd.benefit.Model;

/**
 * Created by SAM33R on 28-05-2018.
 */

public class ResponseForUpdate {

    private String message;
    public Boolean success;


    public class Data{
        private String _id;
        private String name;
        private int __v;
        private int age;
        private String gender;


        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int get__v() {
            return __v;
        }

        public void set__v(int __v) {
            this.__v = __v;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

       /* public class Measurement{
            public int height;
            public int weight;

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public int getWeight() {
                return weight;
            }

            public void setWeight(int weight) {
                this.weight = weight;
            }
        }
        public Measurement measurements;*/


    }


    public Data data;
}
