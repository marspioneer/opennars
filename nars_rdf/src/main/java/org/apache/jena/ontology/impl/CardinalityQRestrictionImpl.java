/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Package
///////////////
package org.apache.jena.ontology.impl;


// Imports
///////////////

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.*;


/**
 * <p>
 * Implementation of the exact qualified cardinality restriction
 * </p>
 */
public class CardinalityQRestrictionImpl
    extends QualifiedRestrictionImpl
    implements CardinalityQRestriction
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating QualifiedRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link org.apache.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new CardinalityQRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to CardinalityQRestriction");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg )
            { return isCardinalityQRestriction( node, eg ); }
    };

    public static boolean isCardinalityQRestriction( Node node, EnhGraph eg ) {
        // node will support being a QualifiedRestriction facet if it has rdf:type owl:Restriction or equivalent
        Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
        return (profile != null)  &&  profile.isSupported( node, eg, CardinalityQRestriction.class );
    }

    @Override
    public boolean isValid()
        { return isCardinalityQRestriction( asNode(), getGraph() ); }

    // Instance variables
    //////////////////////////////////

    /**
     * <p>
     * Construct a qualified restriction node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public CardinalityQRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that this restriction restricts the property to have the given
     * cardinality. Any existing statements for <code>cardinalityQ</code>
     * will be removed.</p>
     * @param cardinality The cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#CARDINALITY_Q()} property is not supported in the current language profile.
     */
    @Override
    public void setCardinalityQ( int cardinality ) {
        setPropertyValue( getProfile().CARDINALITY_Q(), "CARDINALITY_Q", getModel().createTypedLiteral( cardinality ) );
    }

    /**
     * <p>Answer the cardinality of the restricted property.</p>
     * @return The cardinality of the restricted property
     * @exception ProfileException If the {@link Profile#CARDINALITY_Q()} property is not supported in the current language profile.
     */
    @Override
    public int getCardinalityQ() {
        return objectAsInt( getProfile().CARDINALITY_Q(), "CARDINALITY_Q" );
    }

    /**
     * <p>Answer true if this property restriction has the given cardinality.</p>
     * @param cardinality The cardinality to test against
     * @return True if the given cardinality is the cardinality of the restricted property in this restriction
     * @exception ProfileException If the {@link Profile#CARDINALITY_Q()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasCardinalityQ( int cardinality ) {
        return hasPropertyValue( getProfile().CARDINALITY_Q(), "CARDINALITY_Q", getModel().createTypedLiteral( cardinality ) );
    }

    /**
     * <p>Remove the statement that this restriction has the given cardinality
     * for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cardinality A cardinality value to be removed from this restriction
     * @exception ProfileException If the {@link Profile#CARDINALITY_Q()} property is not supported in the current language profile.
     */
    @Override
    public void removeCardinalityQ( int cardinality ) {
        removePropertyValue( getProfile().CARDINALITY_Q(), "CARDINALITY_Q", getModel().createTypedLiteral( cardinality ) );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
