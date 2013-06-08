/**
 * File: Isodata.java
 * Programmer: Scott Deuser
 * Date Started: June 11, 2008
 * Last Updated: August 15, 2008
 * Description - This class is a subclass of the UnsupervisedClustering class. It extends that 
 *               class and contains the methods and tools for performing an ISODATA clustering
 *               on a set of data.
 * Intended Use - To perform clustering using the ISODATA algorithm following the steps outlined
 *                in "Pattern Recognition Principles" by J. T. Tou and R. C. Gonzalez.
 */

package edu.illinois.ncsa.isda.clustering;

import java.util.ArrayList;
import java.util.Collections;

public class Isodata extends UnsupervisedClustering {

    //private class, used as a structure to hold information.
    //Used to keep track of two cluster centers and the distance between them.
    private class ClusterCenterDistances implements
            Comparable<ClusterCenterDistances> {
        public int    index1;  //index 
        public int    index2;  //index
        public double distance;

        ClusterCenterDistances(int i, int j, double distance) {
            this.index1 = i;
            this.index2 = j;
            this.distance = distance;
        }

        /**
         * implements comparable so an ArrayList of ClusterCenterDistances can
         * be sorted.
         * 
         * @param c
         *            - cluster center distances
         * @return - -1 if this distance is less than the the parameter distance
         *         - 0 if this distance is greater than the parameter distance -
         *         1 if they are equal
         */
        public int compareTo(ClusterCenterDistances c) {
            if (this.distance < c.distance) {
                return -1;
            } else if (this.distance > c.distance) {
                return 0;
            } else {
                return 1;
            }
        } //end compareTo()
    } //end ClusterCenterDistances()

    private int    thetaN;     // minimum number of points in cluster
    private double thetaS;     // standard deviation parameter
    private int    thetaC;     // lumping parameter 
    private int    maxLump;    // maximum number of pairs of cluster centers which can be lumped
    private double gammaFactor; // Parameter used for splitting. Multiply this parameter by a standard deviation to get gamma.

    /**
     * Constructor - sets default values to parameters
     */
    public Isodata() {
        super();
        this.thetaN = 1;
        this.thetaS = 1;
        this.thetaC = 4;
        this.maxLump = 0;
        this.gammaFactor = .5;
    } //end IsodataAlgorithm

    /**
     * Constructor
     * 
     * @param numClusters
     *            - number of clusters desired
     * @param thetaN
     *            - minimum number of points in cluster
     * @param thetaS
     *            - standard deviation parameter
     * @param thetaC
     *            - lumping parameter
     * @param maxLump
     *            - maximum number of pairs of cluster centers which can be
     *            lumped
     * @param maxIterations
     *            - number of iterations allowed
     * @param gammaFactor
     *            - Parameter used for splitting. Multiply this parameter by a
     *            standard deviation to get gamma.
     */
    public Isodata(int numClusters, int thetaN, int thetaS, int thetaC,
            int maxLump, int maxIterations, double gammaFactor) {
        super(numClusters, maxIterations);
        this.thetaN = thetaN;
        this.thetaS = thetaS;
        this.thetaC = thetaC;
        this.maxLump = maxLump;
        this.gammaFactor = gammaFactor;
    } //end IsodataAlgorithm()

    /**
     * @return the thetaN
     */
    public int getThetaN() {
        return thetaN;
    }//end getThetaN()

    /**
     * @param thetaN
     *            the thetaN to set
     */
    public boolean setThetaN(int thetaN) {
        if (thetaN < 0) {
            return false;
        }

        this.thetaN = thetaN;
        return true;
    }//end setThetaN()

    /**
     * @return the thetaS
     */
    public double getThetaS() {
        return thetaS;
    }//end getThetaS()

    /**
     * @param thetaS
     *            the thetaS to set
     */
    public boolean setThetaS(double thetaS) {
        if (thetaS < 0) {
            return false;
        }

        this.thetaS = thetaS;
        return true;
    }//end setThetaS()

    /**
     * @return the thetaC
     */
    public int getThetaC() {
        return thetaC;
    }//end getThetaC()

    /**
     * @param thetaC
     *            the thetaC to set
     */
    public boolean setThetaC(int thetaC) {
        if (thetaC < 0) {
            return false;
        }

        this.thetaC = thetaC;
        return true;
    }//end setThetaC()

    /**
     * @return the maxLump
     */
    public int getMaxLump() {
        return maxLump;
    } //end getMaxLump()

    /**
     * @param maxLump
     *            - the maxLump to set
     */
    public boolean setMaxLump(int maxLump) {
        if (maxLump < 0) {
            return false;
        }

        this.maxLump = maxLump;
        return true;
    } //end setMaxLump()

    /**
     * @return the gammaFactor
     */
    public double getGammaFactor() {
        return gammaFactor;
    }//end getGammaFactor()

    /**
     * @param gammaFactor
     *            the gammaFactor to set
     */
    public void setGammaFactor(double gammaFactor) {
        this.gammaFactor = gammaFactor;
    }//end setGammaFactor()

    /**
     * @return the numberOfDimensions
     */
    public int getNumberOfDimensions() {
        return super.numberOfDimensions;
    }//end getNumberOfDimensions()

    /**
     * Finds the first "numCluster" points that are at least 2*StDev = 2*thetaS
     * away from each other and makes them the initial cluster centers. Note:
     * this method of finding seeds is unique to the Isodata Algorithm.
     * 
     * @return - The points that are the initial centers.
     * @throws Exception
     *             is thrown if the set of points has not been set up yet.
     */
    public ArrayList<Point> twoTimesStDev() throws Exception {
        if (super.setOfPoints == null) {
            throw new Exception("There are no points in which to find seeds");
        }

        super.clusterList.clear();
        boolean isThisBadPoint = false;
        int index = 0;
        int indexShift = 0;
        //add the first point
        super.clusterList.add(new Cluster(super.setOfPoints.get(index++)));

        double distance = 0;
        Point p1;
        Point p2;

        while ((index < super.numClusters)
                && ((index + indexShift) < super.setOfPoints.size())) {
            //make sure a point is at least 2*StDev(thetaS) away from all other points
            for (int j = 0; j < index; j++) {
                p1 = super.clusterList.get(j).getClusterCenter();
                p2 = super.setOfPoints.get(index + indexShift);
                distance = super.calculateDistance(p1, p2);
                if ((2 * this.thetaS) > distance) {
                    isThisBadPoint = true;
                }
            }
            if (isThisBadPoint) {
                //try the next point
                indexShift++;
                isThisBadPoint = false;
            } else {
                super.clusterList.add(new Cluster(super.setOfPoints.get(index
                        + indexShift)));
                index++;
                isThisBadPoint = false;
            }
        } //end while
        return super.getClusterCenters();
    } //end twoTimesStDev()

    /**
     * Performs ISODATA clustering on the points given the set parameters.
     * 
     * @return - A list of Clusters
     */
    public ArrayList<Cluster> runIsodataAlgorithm() throws Exception {
        int step = 2; //what step to go to 
        double[] avgDj = null; //Average distance of each cluster subset from its center
        double avgDistance = 0; //Overall average distance of the distance from points to cluster centers
        double[][] sigma_j; //Standard deviation vector	
        int[] sigmaMaxIndices; //the index of the maximum component in each sigma_j in the standard deviation vector
        ArrayList<ClusterCenterDistances> centerDistances; //pairwise distances between all cluster centers
        ArrayList<ClusterCenterDistances> smallerThanThetaC; //center distances that are smaller than thetaC
        boolean clusterCentersEqual = false; //if there were no changes in cluster centers from one iteration to
        //the next, then this is true. If there were changes, this is false.
        super.currentIteration = 1;

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

        while ((super.currentIteration <= super.maxIterations)
                && (!clusterCentersEqual)) {
            switch (step) {
            case 2:
                //Step 2: Distribute the N samples among the present cluster Centers.
                super.distributeSamples();
                super.currentIteration++;

                //Step 3: Discard sample subsets with fewer than thetaN members.
                discardAnySubsets(super.clusterList);

                //Step 4: Update each cluster center. 
                super.clusterListOld = super.calculateClusterCenters();
                //take the old cluster centers and compare them to the new cluster centers.
                clusterCentersEqual = checkCentersEqual(super.clusterListOld,
                        super.clusterList);

                //Step 5: Compute average distance avgDj.
                avgDj = computeAverageDistance(super.clusterList);

                //How does Step 3 affect this calculation?
                //Step 6: Compute the overall average distance.
                avgDistance = computerOverallAverageDistance(super.clusterList,
                        avgDj);

                //Step 7: 
                //a) If last iteration
                if (super.currentIteration == this.maxIterations) {
                    thetaC = 0;
                    step = 11;
                }
                //b) 
                else if (super.clusterList.size() <= super.numClusters / 2) {
                    step = 8;
                } else if (super.clusterList.size() >= 2 * super.numClusters) {
                    step = 11;
                } else {
                    //otherwise, continue
                    step = 8;
                }
                break;
            case 8:

                //Step 8: Find standard deviation vector sigmaJ
                sigma_j = computeStandardDeviation(super.clusterList);

                //Step 9: Find the maximum component of each sigma in sigma_j
                sigmaMaxIndices = findMaxIndices(sigma_j);

                //Step 10: Decides is clusters should be split.
                //if there was splitting go to step 2
                if (splitClusters(super.clusterList, avgDistance, avgDj,
                        sigma_j, sigmaMaxIndices)) {
                    step = 2;
                } else {
                    step = 11;
                }
                break;

            case 11:
                //Step 11: computes pairwise distances between cluster centers
                centerDistances = computeCenterDistances(super.clusterList);

                //Step 12: Compares center distances against thetaC. Arranges L of them less than thetaC in ascending order.
                smallerThanThetaC = compareCenterDistances(super.clusterList,
                        centerDistances);

                //Step 13: Lumps clusters together
                lumpClusters(super.clusterList, smallerThanThetaC);

                //oldClusterList = currentClusterList;
                step = 2;

                break;
            }//end switch
        }//end while

        return super.clusterList;
    } //end runIsodataAlgorithm()

    /**
     * Looks at the number of points in each cluster. If the number of points in
     * a particular cluster is less than thetaN, then that cluster is discarded.
     * 
     * @param subsets
     *            - The cluster list.
     */
    private void discardAnySubsets(ArrayList<Cluster> subsets) {
        ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();

        //clusters can not be removed during this loop without the over head of an index shift 
        for (int i = 0; i < subsets.size(); i++) {
            if (subsets.get(i).getNumberOfPointsInCluster() < thetaN) {
                indicesToRemove.add(i);
            }
        }

        for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
            subsets.remove(indicesToRemove.get(i).intValue());
        }
    } //end discardAnySubsets()

    /**
     * Computes the average distance avgDj of samples in clusters from their
     * corresponding cluster center.
     * 
     * @param clusterList
     *            - the list of clusters
     * @return - the array of average distances that were computed
     */
    private double[] computeAverageDistance(ArrayList<Cluster> clusterList) {
        double[] averageDistance = new double[clusterList.size()];
        double sum = 0;
        int Nj; //number of points in the jth cluster
        //loop through all clusters
        for (int j = 0; j < clusterList.size(); j++) {
            Nj = clusterList.get(j).getNumberOfPointsInCluster();
            //sum up distances for a particular cluster
            try {
                for (int i = 0; i < Nj; i++) {
                    sum += super.calculateDistance(clusterList.get(j).getPoint(
                            i), clusterList.get(j).getClusterCenter());
                }
            } catch (Exception e) {
                System.out
                        .println("Error indexing in IsodataAlgorithm.computeAverageDistance()");
            }
            averageDistance[j] = sum / Nj;
            sum = 0;
        }
        return averageDistance;
    } //end computeAverageDistance()

    /**
     * Computes the overall average distance of the samples from their
     * respective cluster centers.
     * 
     * @param clusterList
     *            - the list of clusters
     * @param avgDj
     *            - array of averages of each cluster
     * @return - the overall average distance
     */
    private double computerOverallAverageDistance(
            ArrayList<Cluster> clusterList, double[] avgDj) {
        int N = setOfPoints.size();
        double sum = 0;
        for (int j = 0; j < clusterList.size(); j++) {
            sum += (clusterList.get(j).getNumberOfPointsInCluster() * avgDj[j]);
        }
        return (sum / N);
    } //end computeOverallAverageDistance()

    /**
     * Computes the standard deviation vector, sigma_j by computing the standard
     * deviation vector for each cluster, for j = 1, 2, ..., Nc
     * 
     * @param clusterList
     *            - list of clusters
     * @return - sigma_j
     */
    private double[][] computeStandardDeviation(ArrayList<Cluster> clusterList) {
        int Nj; //number of points in the jth cluster
        double[][] sigma = new double[clusterList.size()][this.numberOfDimensions];
        double sum = 0;

        //find standard deviation for each cluster
        for (int j = 0; j < clusterList.size(); j++) {
            Nj = clusterList.get(j).getNumberOfPointsInCluster();
            //i iterates over the dimensions, the ith dimension
            for (int i = 0; i < this.numberOfDimensions; i++) {
                //k iterates over the points, the kth point
                for (int k = 0; k < Nj; k++) {
                    try {
                        sum += (Math.pow((clusterList.get(j).getPoint(k)
                                .getOrdinate(i) - clusterList.get(j)
                                .getClusterCenter().getOrdinate(i)), 2));
                    } catch (Exception e) {
                        System.out
                                .println("Indexing error in IsodataAlgorithm.computeStandardDeviation");
                    }
                }
                sigma[j][i] = Math.sqrt(sum / Nj);
                sum = 0;
            }
        }
        return sigma;
    } //end computerStandardDeviation()

    /**
     * Finds the index of the maximum value in each array.
     * 
     * @param list
     *            - a list of arrays that contain doubles, all of the same
     *            length
     * @return - an array of indices
     */
    private int[] findMaxIndices(double[][] list) {
        int[] maxIndices = new int[list.length];
        int currentMaxIndex = 0;
        for (int j = 0; j < list.length; j++) {
            for (int i = 1; i < list[j].length; i++) {
                if (list[j][currentMaxIndex] < list[j][i]) {
                    currentMaxIndex = i;
                }
            }
            maxIndices[j] = currentMaxIndex;
            currentMaxIndex = 0;
        }
        return maxIndices;
    } //end findSigmaMaxIndicies()

    /**
     * Based on values of parameters, decides if a center should be split.
     * Updates the current number of clusters.
     * 
     * @param clusterList
     *            - the list of clusters
     * @param avgDistance
     *            - overall average distance of the points from their respective
     *            centers
     * @param avgDj
     *            - average distance of the points from their respective centers
     * @param Nc
     *            - current number of clusters
     * @param sigma_j
     *            - Standard deviation vector.
     * @param sigmaMaxIndices
     *            - Indices of maximum deviations for each sigma_j
     */
    private boolean splitClusters(ArrayList<Cluster> clusterList,
            double avgDistance, double[] avgDj, double[][] sigma_j,
            int[] sigmaMaxIndices) throws Exception {
        double gamma = 0; //added to cluster center and subtracted from cluster center to get new cluster centers
        int numberOfSplits = 0; //indicates how many splits took place, also needed for index shifting
        int Nc = super.clusterList.size();
        //loop through each cluster
        for (int j = 0; j < Nc; j++) {
            if ((sigma_j[j][sigmaMaxIndices[j]] > thetaS)
                    && (((avgDj[j] > avgDistance) && (clusterList.get(
                            j + numberOfSplits).getNumberOfPointsInCluster() > 2 * (thetaN + 1))) || (Nc <= super.numClusters / 2))) {
                gamma = gammaFactor * sigma_j[j][sigmaMaxIndices[j]];
                splittingHandler(clusterList, j + numberOfSplits++, gamma,
                        sigmaMaxIndices[j]);
            }
        }
        return (numberOfSplits > 0);
    } //end splitClusters()

    /**
     * Handles the calculations in making two new cluster centers from a
     * previous cluster center. Updates the cluster list as well. Two new
     * clusters are placed in same area as where the old one was.
     * 
     * @param clusterList
     *            - the list of clusters
     * @param index
     *            - the index of the cluster to be split
     * @param gamma
     *            - the amount to change the center
     */
    private void splittingHandler(ArrayList<Cluster> clusterList, int index,
            double gamma, int indexOfMax) {
        Point oldCenter = new Point(clusterList.get(index).getClusterCenter());
        Point newPointMinus = new Point(oldCenter);
        Point newPointPlus = new Point(oldCenter);

        //calculate and store new ordinate values for the new points
        try {
            newPointMinus.setOneOrdinate(oldCenter.getOrdinate(indexOfMax)
                    - gamma, indexOfMax);
            newPointPlus.setOneOrdinate(oldCenter.getOrdinate(indexOfMax)
                    + gamma, indexOfMax);
        } catch (Exception e) {
            System.out
                    .println("Error indexing ordinates of a Point in IsodataAlgorithm.splittingHandler");
        }

        //get rid of old cluster
        clusterList.remove(index);

        //squeeze the two new ones in the same area
        clusterList.add(index, new Cluster(newPointMinus));
        clusterList.add(index + 1, new Cluster(new Point(newPointPlus)));

    } //end splittingHandler()

    /**
     * Computes the pairwise distances between all cluster centers.
     * 
     * @param clusterList
     *            - the list of clusters
     * @return - the distances between pairs of clusters
     */
    private ArrayList<ClusterCenterDistances> computeCenterDistances(
            ArrayList<Cluster> clusterList) {
        ArrayList<ClusterCenterDistances> centerDistances = new ArrayList<ClusterCenterDistances>();
        double distance;
        int Nc = clusterList.size();
        for (int i = 0; i < Nc - 1; i++) {
            for (int j = i + 1; j < Nc; j++) {

                distance = super.calculateDistance(clusterList.get(i)
                        .getClusterCenter(), clusterList.get(j)
                        .getClusterCenter());
                centerDistances.add(new ClusterCenterDistances(i, j, distance));
            }
        }
        return centerDistances;
    } //end centerDistances()

    /**
     * Compares the pairwise distances against thetaC. Arranges the L smallest
     * distances which are less than thetaC in ascending order.
     * 
     * @param clusterList
     *            - list of clusters
     * @param distances
     *            - list of pairwise distances between all clusters
     * @return - the list of distances less than thetaC with a size less than or
     *         equal to L
     */
    private ArrayList<ClusterCenterDistances> compareCenterDistances(
            ArrayList<Cluster> clusterList,
            ArrayList<ClusterCenterDistances> distances) {
        ArrayList<ClusterCenterDistances> lesserDistances = new ArrayList<ClusterCenterDistances>();

        for (int i = 0; i < distances.size(); i++) {
            if (distances.get(i).distance < this.thetaC) {
                lesserDistances.add(distances.get(i));
            }
        }

        //ClusterCenterDistances implements Comparable, so it can be sorted (should be in ascending order)
        Collections.sort(lesserDistances);

        //there can only be a maximum of L entries in lesserDistances
        while (lesserDistances.size() > this.maxLump) {
            lesserDistances.remove(lesserDistances.size() - 1);
        }

        return lesserDistances;
    } //end compareCenterDistances()

    /**
     * This method takes care of the bookkeeping for lumping clusters together.
     * 
     * @param clusterList
     *            - the list of clusters
     * @param distances
     *            - list of ClusterCenterDistances that contain the index of the
     *            two clusters and their center distance between each other
     * @return - the number of clusters that were lumped together
     */
    private void lumpClusters(ArrayList<Cluster> clusterList,
            ArrayList<ClusterCenterDistances> distances) throws Exception {
        int numberOfLumps = 0; //keeps track of number of lumps 
        int distancesIndex = 0;
        ArrayList<Integer> clustersToDelete = new ArrayList<Integer>();
        while (distancesIndex < distances.size()) {
            this.lumpHandler(clusterList, distances.get(distancesIndex).index1,
                    distances.get(distancesIndex).index2);
            numberOfLumps++;
            clustersToDelete.add(distances.get(distancesIndex).index1);
            clustersToDelete.add(distances.get(distancesIndex).index2);
            //get rid of any items in "distances" that refer to any of the clusters that were just lumped
            for (int i = distancesIndex + 1; i < distances.size(); i++) {
                if ((distances.get(distancesIndex).index1 == distances.get(i).index1)
                        || (distances.get(distancesIndex).index1 == distances
                                .get(i).index2)) {
                    distances.remove(i);
                } else if ((distances.get(distancesIndex).index2 == distances
                        .get(i).index1)
                        || (distances.get(distancesIndex).index2 == distances
                                .get(i).index2)) {
                    distances.remove(i);
                }
            }
            distancesIndex++;
        }

        //delete clusters that were lumped
        int indexShift = 0;
        Collections.sort(clustersToDelete);
        for (int i = 0; i < clustersToDelete.size(); i++) {
            clusterList.remove(clustersToDelete.get(i) + indexShift++);
        }

    } //end lumpClusters()

    /**
     * Creates the new cluster center from the two old cluster centers.
     * 
     * @param clusterList
     *            - the list of clusters
     * @param index1
     *            - the index of a cluster to lump
     * @param index2
     *            - the index of the other cluster to lump
     */
    private void lumpHandler(ArrayList<Cluster> clusterList, int index1,
            int index2) {
        double[] newClusterCenter = new double[this.numberOfDimensions];
        double numOfPoints1 = clusterList.get(index1)
                .getNumberOfPointsInCluster();
        double numOfPoints2 = clusterList.get(index2)
                .getNumberOfPointsInCluster();
        double ordinateValue1;
        double ordinateValue2;
        for (int i = 0; i < this.numberOfDimensions; i++) {
            try {
                ordinateValue1 = clusterList.get(index1).getClusterCenter()
                        .getOrdinate(i);
                ordinateValue2 = clusterList.get(index2).getClusterCenter()
                        .getOrdinate(i);
                newClusterCenter[i] = (1 / (numOfPoints1 + numOfPoints2))
                        * ((numOfPoints1 * ordinateValue1) + (numOfPoints2 * ordinateValue2));
            } catch (Exception e) {
                System.out
                        .println("Index out of bounds in IsodataAlgorithm.lumpHandler");
            }
        }

        //put new point at the end of the clusterList
        clusterList.add(new Cluster(new Point(newClusterCenter)));
    } //end lumpHandler()		

} //end IsodataAlgorithm{}
