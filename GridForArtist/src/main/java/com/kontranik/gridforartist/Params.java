package com.kontranik.gridforartist;

public class Params {
        float left;
        float top;
        float width;
        float height;
        float scaleX;
        float scaleY;
        float skewX;
        float skewY;

        public  Params() {}

        public Params(float left, float top, float width, float height, float scaleX, float scaleY, float skewX, float skewY) {
                this.left = left;
                this.top = top;
                this.width = width;
                this.height = height;
                this.scaleX = scaleX;
                this.scaleY = scaleY;
                this.skewX = skewX;
                this.skewY = skewY;
        }
}
