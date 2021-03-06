/*
 * This file is part of ReplayStudio, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 johni0702 <https://github.com/johni0702>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.replaymod.replaystudio.pathing.property;

import org.apache.commons.lang3.tuple.Triple;

public class PropertyParts {
    private PropertyParts(){}

    public static class ForInteger extends AbstractPropertyPart<Integer> {
        public ForInteger(Property<Integer> property, boolean interpolatable) {
            super(property, interpolatable);
        }

        public ForInteger(Property<Integer> property, boolean interpolatable, int upperBound) {
            super(property, interpolatable, upperBound);
        }

        @Override
        public double toDouble(Integer value) {
            return value;
        }

        @Override
        public Integer fromDouble(Integer value, double d) {
            return (int) Math.round(d);
        }
    }

    public static class ForDoubleTriple extends AbstractPropertyPart<Triple<Double, Double, Double>> {
        private final TripleElement element;
        public ForDoubleTriple(Property<Triple<Double, Double, Double>> property, boolean interpolatable, TripleElement element) {
            super(property, interpolatable);
            this.element = element;
        }

        public ForDoubleTriple(Property<Triple<Double, Double, Double>> property, boolean interpolatable, double upperBound, TripleElement element) {
            super(property, interpolatable, upperBound);
            this.element = element;
        }

        @Override
        public double toDouble(Triple<Double, Double, Double> value) {
            switch (element) {
                case LEFT: return value.getLeft();
                case MIDDLE: return value.getMiddle();
                case RIGHT: return value.getRight();
            }
            throw new AssertionError(element);
        }

        @Override
        public Triple<Double, Double, Double> fromDouble(Triple<Double, Double, Double> value, double d) {
            switch (element) {
                case LEFT: return Triple.of(d, value.getMiddle(), value.getRight());
                case MIDDLE: return Triple.of(value.getLeft(), d, value.getRight());
                case RIGHT: return Triple.of(value.getLeft(), value.getMiddle(), d);
            }
            throw new AssertionError(element);
        }
    }

    public static class ForFloatTriple extends AbstractPropertyPart<Triple<Float, Float, Float>> {
        private final TripleElement element;
        public ForFloatTriple(Property<Triple<Float, Float, Float>> property, boolean interpolatable, TripleElement element) {
            super(property, interpolatable);
            this.element = element;
        }

        public ForFloatTriple(Property<Triple<Float, Float, Float>> property, boolean interpolatable, float upperBound, TripleElement element) {
            super(property, interpolatable, upperBound);
            this.element = element;
        }

        @Override
        public double toDouble(Triple<Float, Float, Float> value) {
            switch (element) {
                case LEFT: return value.getLeft();
                case MIDDLE: return value.getMiddle();
                case RIGHT: return value.getRight();
            }
            throw new AssertionError(element);
        }

        @Override
        public Triple<Float, Float, Float> fromDouble(Triple<Float, Float, Float> value, double d) {
            switch (element) {
                case LEFT: return Triple.of((float) d, value.getMiddle(), value.getRight());
                case MIDDLE: return Triple.of(value.getLeft(), (float) d, value.getRight());
                case RIGHT: return Triple.of(value.getLeft(), value.getMiddle(), (float) d);
            }
            throw new AssertionError(element);
        }
    }

    public enum TripleElement {
        LEFT, MIDDLE, RIGHT;
    }
}
