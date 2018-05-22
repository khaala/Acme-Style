package repositories;

import domain.Servise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ServiseRepository  extends JpaRepository<Servise,Integer>{
    @Query("select s from Servise s where s.publicationDate <= CURRENT_TIMESTAMP ")
    Collection<Servise> servisesPublished();
}
