/**
 * File: Cluster.java
 * Programmer: Scott Deuser
 * Date Started: June 6, 2008
 * Last Updated: August 15, 2008
 * Description - This class is meant to be a data structure that should contain
 *               all information needed about a cluster. This includes the cluster center
 *               which is a point and also a list of all points in the cluster. 
 * Intended Use - Used with clustering algorithms. Designed while coding
 *                the k-means and ISODATA clustering algorithms.
 */

package edu.uiuc.ncsa.isda.clustering;

import java.util.ArrayList;

public class Cluster {

    //private data members 
    private Point            clusterCenter;
    private ArrayList<Point> pointsInCluster;

    //constructors
    /**
     * Constructor
     * 
     * param clusterCenter - center point of the cluster
     */
    public Cluster(Point clusterCenter) {
        this(clusterCenter, new ArrayList<Point>());
    }//end Cluster()

    /**
     * constructor
     * 
     * @param clusterCenter
     *            - center point of the cluster
     * @param points
     *            - list of all the points in the cluster
     */
    public Cluster(Point clusterCenter, ArrayList<Point> points) {
        this.clusterCenter = new Point(clusterCenter);
        this.pointsInCluster = points;

    }//end Cluster()

    /**
     * copy constructor makes a deep copy of the Cluster
     * 
     * @param c
     */
    public Cluster(Cluster c) {
        this.clusterCenter = new Point(c.clusterCenter);
        this.pointsInCluster = new ArrayList<Point>();
        for (int i = 0; i < c.pointsInCluster.size(); i++)
            this.pointsInCluster.add(c.pointsInCluster.get(i));
    }//end Cluster()

    /**
     * 
     * sets a new center for the cluster
     * 
     * @param newCenter
     *            - the point of the new center
     */
    public void setClusterCenter(Point newCenter) {
        this.clusterCenter = new Point(newCenter);
    }//end setClusterCenter()

    /**
     * sets a new center for the cluster
     * 
     * @param ordinates
     *            - the ordinates of the new cluster center
     * @throws Exception
     *             - throws exception if index out of bounds when setting
     *             ordinates
     */
    public void setClusterCenter(double[] ordinates) throws Exception {
        for (int i = 0; i < ordinates.length; i++)
            this.clusterCenter.setOneOrdinate(ordinates[i], i);
    }//end setClusterCenter()

    /**
     * adds a Point to the cluster
     * 
     * @param p
     *            - point to be added
     */
    public void addPointToCluster(Point p) {
        this.pointsInCluster.add(p);
    }//end addPointToCluster()

    /**
     * adds all the points to the cluster
     * 
     * @param p
     *            - array of points to be added to the cluster
     */
    public void addPointsToCluster(Point[] p) {
        for (int i = 0; i < p.length; i++)
            this.pointsInCluster.add(p[i]);
    }//end addPointsToCluster()

    /**
     * adds a point to the cluster
     * 
     * @param p
     *            - point added to the cluster
     */
    public void addPointsToCluster(ArrayList<Point> p) {
        for (int i = 0; i < p.size(); i++)
            this.pointsInCluster.add(p.get(i));
    }//end addPointsToCluster()

    /**
     * removes the point at the specified index
     * 
     * @return - point removed
     */
    public Point removePoint(int index) throws Exception {
        if ((index < 0) || (index >= this.pointsInCluster.size()))
            throw new IndexOutOfBoundsException("Index out of bounds");
        return this.pointsInCluster.remove(index);
    }//end removePoint()

    /**
     * removes all the points from the set. Not including the center
     */
    public void clearPoints() {
        this.pointsInCluster.clear();
    }//end clearPoints()

    /**
     * returns the point at the given index
     * 
     * @param index
     *            - index of the point wanted
     * @return Point - the point at that index
     * @throws Exception
     *             - throws if index is out of bounds
     */
    public Point getPoint(int index) throws Exception {
        if (this.pointsInCluster == null || (index < 0)
                || (index > this.pointsInCluster.size()))
            throw new IndexOutOfBoundsException("Index out of bounds");
        else
            return this.pointsInCluster.get(index);
    }//end getOneValue()

    /**
     * returns the cluster center
     * 
     * @return - the cluster center
     */
    public Point getClusterCenter() {
        return this.clusterCenter;
    }//end getClusterCenter()

    /**
     * returns the number of points in the cluster
     * 
     * @return - number of points in the cluster
     */
    public int getNumberOfPointsInCluster() {
        return this.pointsInCluster.size();
    }//end getNumberOfPoinsInCluster

    /**
     * checks to see if there are any points in set
     * 
     * @return - true if empty false otherwise
     */
    public boolean isEmpty() {
        return ((this.pointsInCluster.size() == 0) ? true : false);
    }

    /**
     * checks all the points in both of the Clusters and checks both Clusters'
     * centers
     * 
     * @param c
     *            - incoming cluster
     * @return - true if all points (including the center) are equal false
     *         otherwise
     */
    public boolean equals(Cluster c) {
        //check to see if both have the same number of points and
        //check to see if centers are equal
        if ((this.pointsInCluster.size() != c.pointsInCluster.size())
                || (!this.clusterCenter.equals(c.clusterCenter)))
            return false;

        //check that all points in both clusters are equal
        //Note: they can be in different order

        //needed in the case that there are duplicate points 
        boolean[] indeciesFound = new boolean[c.pointsInCluster.size()];
        for (int i = 0; i < indeciesFound.length; i++)
            indeciesFound[i] = false;

        boolean found = false;
        for (int i = 0; i < this.pointsInCluster.size(); i++) {
            int j = 0;
            while ((!found) && (j < this.pointsInCluster.size())) {
                if ((!indeciesFound[j])
                        && this.pointsInCluster.get(i).equals(
                                c.pointsInCluster.get(j))) {
                    indeciesFound[j] = true;
                    found = true;
                }
                j++;
            }
            //if while() did not find point
            if (!found)
                return false;
            found = false; //reset found
        }

        return true;

    }//end equals()

}//end class Cluster
