/**
 * File: Kmeans.java
 * Programmer: Scott Deuser
 * Date Started: June 9, 2008
 * Last Updated: August 15, 2008
 * Description - This class is a subclass of the UnsupervisedClustering class. It extends that 
 *               class and contains the methods and tools for performing an k-means clustering
 *               on a set of data.
 * Intended Use - To perform clustering using the k-means algorithm following the steps outlined
 *                in "Pattern Recognition Principles" by J. T. Tou and R. C. Gonzalez.
 */

package edu.illinois.ncsa.isda.clustering;

import java.util.ArrayList;

public class Kmeans extends UnsupervisedClustering {

    /**
     * Constructor - sets default values to parameters
     */
    public Kmeans() {
        super();
    } //end IsodataAlgorithm	

    /**
     * Constructor
     * 
     * @param numClusters
     *            - number of cluster centers
     * @param maxIterations
     *            - maximum number of iterations that the algorithm will run.
     */
    public Kmeans(int numClusters, int maxIterations) {
        super(numClusters, maxIterations);
    }//end Kmeans()

    /**
     * @return the numberOfDimensions
     */
    public int getNumberOfDimensions() {
        return super.numberOfDimensions;
    }//end getNumberOfDimensions()

    /**
     * Uses the numClusters-Means algorithm to calculate numClusters clusters
     * and numClusters centers
     * 
     * @return - ArrayList (equal in size to numClusters) of Clusters. Points in
     *         set are distributed to the numClusters Clusters.
     */
    public ArrayList<Cluster> runAlgorithm() throws Exception {
        boolean clusterCentersEqual = false;

        if (super.setOfPoints == null) {
            throw new Exception("There are no points to run the algorithm on");
        }

        super.numberOfDimensions = setOfPoints.get(0).getNumberOfDimensions();

        //Step 1: Take in the data (which is done) and set initial cluster centers.
        //if the cluster centers have not been set up or if the size does not match up, calculate the centers
        if ((super.clusterList.size() == 0)
                || (super.clusterList.size() > super.numClusters)) {
            this.firstNPoints();
            System.out.println("Default seeds were used");
        }

        //		//Step 1: make "numClusters" cluster centers
        //		super.firstNPoints();

        //check to make sure the number of cluster centers found is equal to the desired amount
        if (super.clusterList.size() != super.numClusters) {
            System.err
                    .println("The number of seeds did not equal the desired amount of clusters\n"
                            + "Kmeans will may or may not perform satisfactorily");
            //throw new Exception("The number of seeds did not equal the desired amount of clusters\n" +
            //	            "Kmeans will not perform correctly");
        }

        while (!clusterCentersEqual
                && (super.currentIteration < super.maxIterations)) {
            //Step 2: Distribute the samples among the numClusters cluster domains.
            super.distributeSamples();
            super.currentIteration++;

            //Step 3: Compute the new cluster centers.
            super.clusterListOld = super.calculateClusterCenters();

            //Step 4: Check to see if the iteration produced any changes compared to the last iteration.
            clusterCentersEqual = checkCentersEqual(super.clusterListOld,
                    super.clusterList);
        }

        super.distributeSamples();
        return super.clusterList;
    }//end runAlgorithm()

}//end class Kmeans{}
