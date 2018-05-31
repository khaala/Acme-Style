package services;

import domain.*;
import javafx.geometry.Pos;
import org.hibernate.annotations.AttributeAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import repositories.PostRepository;
import security.Authority;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

@Service
@Transactional
public class PostService {

    // Managed repository -----------------------------------------------------

    @Autowired
    private PostRepository postRepository;

    // Supporting services ----------------------------------------------------

    @Autowired
    private ActorService actorService;

    @Autowired
    private Validator validator;

    @Autowired
    private ActionService actionService;

    @Autowired
    private CommentService commentService;

    // Constructors -----------------------------------------------------------

    public PostService() {
        super();
    }

    // CRUD methods -----------------------------------------------------------

    public Post create(){
        Post result;

        result = new Post();
        result.setActor(actorService.findByPrincipal());
        result.setCategories(new ArrayList<Category>());
        result.setComments(new ArrayList<Comment>());
        result.setActions(new ArrayList<Action>());

        return result;
    }

    public Post findOne(int id){
        return postRepository.findOne(id);
    }

    public Post findOneToEdit(int id){
        Post res = postRepository.findOne(id);
        Actor actor = actorService.findByPrincipal();

        Assert.isTrue(res.getActor().equals(actor), "Not the creator of this Post");

        return res;
    }

    public Collection<Post> findAll(){
        return postRepository.findAll();
    }

    public void delete(Post post){
        Assert.notNull(post);
        Assert.isTrue(actorService.findByPrincipal().equals(post.getActor()) || actorService.checkRole(Authority.ADMINISTRATOR));

        if(post.isRaffle() && post.getEndDate().after(new Date()))
            Assert.isTrue(post.getComments().isEmpty());

        if(post.isRaffle() && post.getEndDate().before(new Date()))
            Assert.isTrue(post.isHasWinner(), "Raffle must have winner");

        post.setCategories(new ArrayList<Category>());

        for(Comment c : post.getComments()){
            Actor a = c.getActor();
            a.setComments(new ArrayList<Comment>());
            this.actorService.save(a);
        }

        this.commentService.deleteAll(post);

        for(Action a : post.getActions()){
            Actor aux = a.getActor();
            aux.getActions().remove(a);
            this.actorService.save(aux);

        }

        actionService.deleteAll(post.getActions());
        post.setActions(new ArrayList<Action>());

        postRepository.delete(post);
    }

    public Post save(Post post){
        Assert.notNull(post);
        Assert.isTrue(actorService.findByPrincipal().equals(post.getActor()));

        post.setMoment(new Date(System.currentTimeMillis() - 1000));
        if(!post.isRaffle())
            post.setFinalMode(true);
        Post res = postRepository.save(post);

        return res;
    }

    // Other business methods -------------------------------------------------

    public Post reconstructS(final Post postPruned, final BindingResult binding) {
        Post res;

        if (postPruned.getId() == 0) {
            res = this.create();
        } else {
            res = this.findOne(postPruned.getId());
        }

        res.setCategories(postPruned.getCategories());
        res.setTitle(postPruned.getTitle());
        res.setDescription(postPruned.getDescription());
        res.setPicture(postPruned.getPicture());
        res.setRaffle(postPruned.isRaffle());
        res.setReward(postPruned.getReward());
        res.setEndDate(postPruned.getEndDate());
        res.setFinalMode(postPruned.isFinalMode());

        this.validator.validate(res,binding);

        return res;
    }

    public void likePost(Post post){
        post.setLik(post.getLik() + 1);
        this.postRepository.save(post);
    }


    public void substractLikePost(Post post){
        post.setLik(post.getLik() - 1);
        this.postRepository.save(post);
    }

    public void dislikePost(Post post){
        post.setDislike(post.getDislike() + 1);
        this.postRepository.save(post);
    }

    public void substractDislikePost(Post post){
        post.setDislike(post.getDislike() - 1);
        this.postRepository.save(post);
    }

    public void heartPost(Post post){
        post.setHeart(post.getHeart() + 1);
        this.postRepository.save(post);
    }

    public void substractHeartPost(Post post){
        post.setHeart(post.getHeart() - 1);
        this.postRepository.save(post);
    }

    public boolean checkReward(String reward, BindingResult binding) {
        FieldError error;
        String[] codigos;
        boolean result;

        if (reward.isEmpty())
            result = true;
        else
            result = false;
        if (result) {
            codigos = new String[1];
            codigos[0] = "post.reward.invalid";
            error = new FieldError("post", "reward", reward, false, codigos, null, "Must not be blank");
            binding.addError(error);
        }
        return result;
    }

    public boolean checkEndDate(Date endDate, BindingResult binding) {
        FieldError error;
        String[] codigos;
        Date date = new Date();
        boolean result;

        if (endDate != null)
            result = endDate.after(date);
        else
            result = true;
        if (!result) {
            codigos = new String[1];
            codigos[0] = "post.endDate.invalid";
            error = new FieldError("post", "endDate", endDate, false, codigos, null, "Must be in the future");
            binding.addError(error);
        }
        return result;
    }

    public Collection<Post> postFinalModeFalse(){
        return this.postRepository.postFinalModeFalse();
    }

    public Collection<Post> postRaffleByActor( int actorId){
        return this.postRepository.postRaffleByActor(actorId);
    }
}
