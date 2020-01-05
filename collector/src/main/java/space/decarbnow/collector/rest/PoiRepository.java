package space.decarbnow.collector.rest;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import space.decarbnow.collector.entities.MapPoi;

import java.util.List;

/**
 * Copyright (c) 2019 Matthias Steinböck - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by ma on 11/7/19.
 */
@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "poi", path = "poi")
public interface PoiRepository extends PagingAndSortingRepository<MapPoi, Long> {

    @Query("select c from MapPoi c where within(c.position, ?1) = true")
    List<MapPoi> findWithin(Geometry filter);
}
