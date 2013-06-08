/**
 * File: Point.java
 * Programmer: Scott Deuser
 * Date Started: June 6, 2008
 * Last Updated: August 15, 2008
 * Description - This class symbolizes a point in any kind of space. It is not 
 *               limited to spatial dimensions. It uses an ArrayList
 *               to hold each ordinate of the point. Note that the point can
 *               have any number of dimensions.
 * Intended Use - Used with clustering algorithms. Designed while coding
 *                the k-means and ISODATA clustering algorithms. 
 */

package edu.illinois.ncsa.isda.clustering;

import java.util.ArrayList;

public class Point {
    private final ArrayList<Double> ordinates;

    //constructor 
    public Point(double[] ordinates) {
        this.ordinates = new ArrayList<Double>();
        setPoint(ordinates);
    }//end constructor()

    //copy constructor

    /**
     * copy constructor makes a deep copy of the ArrayList
     * 
     * @param p
     */
    public Point(Point p) {
        this.ordinates = new ArrayList<Double>();
        for (int i = 0; i < p.ordinates.size(); i++) {
            this.ordinates.add(p.ordinates.get(i));
        }
    }//end copy constructor()

    /**
     * @return - number of dimensions in the point
     */
    public int getNumberOfDimensions() {
        return this.ordinates.size();
    }//end getNumberOfDimensions()

    /**
     * 
     * @return - the ordinates of the point
     */
    public ArrayList<Double> getOrdinates() {
        return this.ordinates;
    }//end getOrdinates()

    /**
     * returns the ordinate at the specified dimension
     * 
     * @param dimension
     *            - index of ordinate to get
     * @return the value of the specified ordinate
     * @throws Exception
     */
    public double getOrdinate(int dimension) throws Exception {
        if ((dimension < 0) || (dimension >= this.ordinates.size())) {
            throw new Exception("Index out of bounds");
        }
        return this.ordinates.get(dimension);
    }//end getOrdinate()

    /**
     * sets the point
     * 
     * @param ordinates
     *            - new ordinates of the point being set
     */
    public void setPoint(double[] ordinates) {
        this.ordinates.clear(); //make sure nothing carries over to the new list
        for (int i = 0; i < ordinates.length; i++) {
            this.ordinates.add(i, ordinates[i]);
        }
    }//end setPoint()

    /**
     * set the specified ordinate with the specified value
     * 
     * @param value
     *            - value to set the ordinate to
     * @param dimension
     *            - which ordinate to change. Indexes starting at 0.
     * @throws Exception
     *             - when index out of bounds
     */
    public void setOneOrdinate(double value, int dimension) throws Exception {
        if ((dimension < 0) || (dimension >= this.ordinates.size())) {
            throw new Exception("Index out of bounds");
        }
        this.ordinates.set(dimension, value);
    }//end setOneOrdinate()

    /**
     * Checks to see if each ordinate is exactly the same as the respective
     * ordinate in the other point.
     * 
     * @param p
     *            - the other point being compared.
     * @return - true if the two points are exactly the same and false
     *         otherwise.
     */
    public boolean equals(Point p) {
        for (int i = 0; i < this.getNumberOfDimensions(); i++) {
            if (!(Math.abs(this.ordinates.get(i) - p.ordinates.get(i)) == 0)) {
                return false;
            }
        }
        return true;
    }//end equals

    /**
     * Checks to see if each ordinate in a point is within the tolerance of the
     * respective ordinate in the other point.
     * 
     * @param p
     *            - the other point being compared.
     * @param tolerance
     *            - the range which the two points can differ but still be
     *            considered equal.
     * @return - true if each ordinate of the points are equal within the range
     *         of the tolerance. false if each ordinate in the points is not
     *         equal within the range of the tolerance.
     */
    public boolean equals(Point p, double tolerance) {
        for (int i = 0; i < this.getNumberOfDimensions(); i++) {
            if (!(Math.abs(this.ordinates.get(i) - p.ordinates.get(i)) <= tolerance)) {
                return false;
            }
        }
        return true;
    } //end equals()

    /**
     * get ordinate value in string form Ex. "Point [i, j, k]"
     * 
     * @return - string of the ordinates
     */
    @Override
    public String toString() {
        String result = "Point [";

        for (int i = 0; i < ordinates.size(); i++) {
            if (i == 0) {
                result += ordinates.get(i);
            } else {
                result += ", " + ordinates.get(i);
            }
        }

        return result + "]";
    }

}//end class Point{}
