/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.simulation.matching.graphs;// A Java program to find maximal
// Bipartite matching.

class GFG {


    // A DFS based recursive function that
    // returns true if a matching for
    // vertex u is possible
    boolean bpm(boolean bpGraph[][], int u,
                boolean seen[], int matchR[]) {
        // Try every job one by one
        for (int v = 0; v < N; v++) {
            // If applicant u is interested
            // in job v and v is not visited
            if (bpGraph[u][v] && !seen[v]) {

                // Mark v as visited
                seen[v] = true;

                // If job 'v' is not assigned to
                // an applicant OR previously
                // assigned applicant for job v (which
                // is matchR[v]) has an alternate job available.
                // Since v is marked as visited in the
                // above line, matchR[v] in the following
                // recursive call will not get job 'v' again
                if (matchR[v] < 0 || bpm(bpGraph, matchR[v],
                        seen, matchR)) {
                    matchR[v] = u;
                    return true;
                }
            }
        }
        return false;
    }

    // Returns maximum number
    // of matching from M to N
    int maxBPM(boolean bpGraph[][]) {
        // An array to keep track of the
        // applicants assigned to jobs.
        // The value of matchR[i] is the
        // applicant number assigned to job i,
        // the value -1 indicates nobody is assigned.
        int matchR[] = new int[N];

        // Initially all jobs are available
        for (int i = 0; i < N; ++i)
            matchR[i] = -1;

        // Count of jobs assigned to applicants
        int result = 0;
        for (int u = 0; u < M; u++) {
            // Mark all jobs as not seen
            // for next applicant.
            boolean seen[] = new boolean[N];
            for (int i = 0; i < N; ++i)
                seen[i] = false;

            // Find if the applicant 'u' can get a job
            if (bpm(bpGraph, u, seen, matchR))
                result++;
        }
        return result;
    }
    // M is number of applicants
    // and N is number of jobs
    static  int M = 2;
    static  int N = 2;
    // Driver Code
    public static void main(String[] args)
            throws Exception {
        // Let us createNodeOfEClass a bpGraph shown
        // in the above example
//        boolean[][] bpGraph = new boolean[][]{
//                {false, true, true, false, false, false},
//                {true, false, false, true, false, false},
//                {false, false, true, false, false, false},
//                {false, false, true, true, false, false},
//                {false, false, false, false, false, false},
//                {false, false, false, false, false, true}};
        GFG m = new GFG();
        N = M = 3;
        boolean[][] u_0 = new boolean[][]{
                {true, false, false},
                {true, true, true},
                {true, false, false}
        };

        M = 2; N = 3;
        boolean[][] u_1 = new boolean[][]{
                {true, true, true},
                {true, false, false}
        };
        System.out.println("Maximum number of applicants that can" +
                " get job is " + m.maxBPM(u_1));

        M = 2; N = 3;
        boolean[][] u_2 = new boolean[][]{
                {true, false, false},
                {true, false, false}
        };
        System.out.println("Maximum number of applicants that can" +
                " get job is " + m.maxBPM(u_2));

        M = 2; N = 3;
        boolean[][] u_3 = new boolean[][]{
                {true, false, false},
                {true, true, true}
        };
        System.out.println("Maximum number of applicants that can" +
                " get job is " + m.maxBPM(u_3));



    }
}
