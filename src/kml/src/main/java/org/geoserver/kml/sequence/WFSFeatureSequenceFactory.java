/* Copyright (c) 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.kml.sequence;

import java.util.List;

import org.geoserver.kml.KmlEncodingContext;
import org.geoserver.kml.decorator.KmlDecoratorFactory.KmlDecorator;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * Creates a sequence of Placemark objects mapping the vector contents of a layer. 
 * This one is geared towards WFS, as such it ignores styles 
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class WFSFeatureSequenceFactory implements SequenceFactory<Feature> {

    private SimpleFeatureCollection features;

    private List<KmlDecorator> callbacks;

    private KmlEncodingContext context;

    public WFSFeatureSequenceFactory(KmlEncodingContext context) {
        this.context = context;
        this.features = context.getCurrentFeatureCollection();

        // prepare the callbacks
        callbacks = context.getDecoratorsForClass(Placemark.class);
    }

    @Override
    public Sequence<Feature> newSequence() {
        return new FeatureGenerator(context.openIterator(features));
    }

    public class FeatureGenerator implements Sequence<Feature> {

        private FeatureIterator fi;

        public FeatureGenerator(FeatureIterator fi) {
            this.fi = fi;
        }

        @Override
        public Feature next() {
            // already reached the end?
            if (fi == null) {
                return null;
            }

            while (fi.hasNext()) {
                boolean featureRetrieved = false;
                try {
                    // grab the next feature, with a sentinel to tell us whether there was an
                    // exception
                    SimpleFeature sf = (SimpleFeature) fi.next();
                    featureRetrieved = true;
                    context.setCurrentFeature(sf);

                    // only create the basic placemark here, the rest is delegated to decorators
                    Placemark pm = new Placemark();
                    pm.setId(sf.getID());

                    // call onto the decorators
                    for (KmlDecorator callback : callbacks) {
                        pm = (Placemark) callback.decorate(pm, context);
                        if (pm == null) {
                            // we have to skip this one
                            continue;
                        }
                    }

                    return pm;
                } finally {
                    if (!featureRetrieved) {
                        // an exception has occurred, release the feature iterator
                        context.closeIterator(fi);
                    }
                }
            }

            // did we reach the end just now?
            if (!fi.hasNext()) {
                context.closeIterator(fi);
            }
            return null;
        }

    }

}
