
package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import repositories.MessageRepository;

@Service
@Transactional
public class MessageService {


    // Managed repository -----------------------------------------------------

    @Autowired
    private MessageRepository			messageRepository;

    // Supporting services ----------------------------------------------------

    @Autowired
    private FolderService				folderService;
    @Autowired
    private ActorService				actorService;
    @Autowired
    private ConfigurationService	    configurationSystemService;
    @Autowired
    private AdministratorService administratorService;

    // Constructors -----------------------------------------------------------

    public MessageService() {
        super();
    }

    // CRUD methods --------------------------------------------------------------------------------

    public Message create() {
        Message res = new Message();
        res.setPriority("NEUTRAL");
        long milliseconds = System.currentTimeMillis() - 100;
        Date moment = new Date(milliseconds);
        res.setActorSender(this.actorService.findByPrincipal());
        res.setMoment(moment);
        return res;
    }

    public Message findOne(final int messageId) {
        return messageRepository.findOne(messageId);
    }

    public Collection<Message> findAll() {
        return messageRepository.findAll();

    }

    public Message save(final Message message) {
        return this.messageRepository.save(message);
    }

    public void delete(final Message message) {
        //Coprobamos que el mensaje existe
        Assert.isTrue(this.messageRepository.exists(message.getId()));
        Assert.isTrue(message.getFolder().getActor().getId() == this.actorService.findByPrincipal().getId());
        if (message.getFolder().getName().equals("trashbox") && message.getFolder().getSystem())
            this.messageRepository.delete(message);
        else
            this.moveMessage(this.folderService.findActorAndFolder(this.actorService.findByPrincipal().getId(), "trashbox"), message);

    }

    // Other methods ------------------------------------------------------------------

    //m�todo para mover un mensaje de una carpeta a otra
    public void moveMessage(final Folder destinyFolder, final Message msg) {
        // Comprobamos que la carpeta destino pertenece al actor
        final Actor sender = this.actorService.findByPrincipal();
        final Collection<Folder> folders = sender.getFolders();
        Assert.isTrue(folders.contains(destinyFolder));
        msg.setFolder(destinyFolder);
        this.messageRepository.save(msg);
    }

    public void sendMessage(Message message) {
        Assert.isTrue(!message.getActorReceivers().contains(message.getActorSender()));
        Folder folderOutbox;
        Message msg = message.clone();
        folderOutbox = folderService.findActorAndFolder(message.getActorSender().getId(),"outbox");
        msg.setFolder(folderOutbox);
        messageRepository.save(msg);

        Boolean isSpam = this.isSpam(message);
        for (Actor a : message.getActorReceivers()) {
            Message msgRe = message.clone();
            Folder folderInbox;
            for (Folder f : a.getFolders())
                if ((!isSpam && f.getName().equalsIgnoreCase("inbox") && f.getSystem()) || (isSpam && f.getName().equalsIgnoreCase("spambox") && f.getSystem())) {
                    folderInbox = f;
                    msgRe.setFolder(folderInbox);
                    this.messageRepository.save(msgRe);
                    break;
                }
        }
    }

    public void sendMessageNotification(Message message) {
        Folder folderOutbox;
        Message msg = message.clone();

        folderOutbox = folderService.findActorAndFolder(message.getActorSender().getId(),"outbox");
        msg.setFolder(folderOutbox);
        messageRepository.save(msg);
        for (Actor a : message.getActorReceivers()) {
            Message msgRe = message.clone();
            Folder notificationBox;
            notificationBox = folderService.findActorAndFolder(a.getId(),"notificationbox");
            msgRe.setFolder(notificationBox);
            messageRepository.save(msgRe);
        }
    }

    private Boolean isSpam(Message message) {
        Configuration cs = this.configurationSystemService.getCS();
        Collection<String> spams = cs.getTabooWords();
        Boolean isSpam = false;
        String body = message.getBody();
        String subject = message.getSubject();
        body = body.toUpperCase();
        subject = subject.toUpperCase();
        for (final String s : spams)
            if (body.contains(s.toUpperCase()) || subject.contains(s.toUpperCase())) {
                isSpam = true;
                break;
            }

        return isSpam;
    }

    public void reconstruct(Message message, BindingResult binding) {
        FieldError error;
        String[] codigos;

        if (message.getActorReceivers() == null) {
            codigos = new String[1];
            codigos[0] = "message.receives.error";
            error = new FieldError("sendMessage", "actorReceivers", message.getActorReceivers(), false, codigos, null, "");
            binding.addError(error);
        } else {

        }
    }

    public void sendBroadcast(final Message message) {
        Assert.isTrue(actorService.isAdministrator());
        Assert.isTrue(!message.getActorReceivers().contains(message.getActorSender()));
        Folder folderOutbox;
        final Message msg = message.clone();
        for (final Folder f : message.getActorSender().getFolders())
            if (f.getName().equalsIgnoreCase("outbox")) {
                folderOutbox = f;
                msg.setFolder(folderOutbox);
                this.messageRepository.save(msg);
                break;
            }
    }

    public  void notifyParticipationToUser(User user,Event event){
        Message notification;
        Collection<Actor> recievers;
        Folder notificationBox;

        recievers = new ArrayList<>();
        recievers.add(user);
        notificationBox = folderService.findActorAndFolder(user.getId(),"notificationbox");

        notification = new Message();
        notification.setPriority("HIGH");
        notification.setActorReceivers(recievers);
        notification.setFolder(notificationBox);
        notification.setSubject("Confirmation");
        notification.setBody("Your participation to "+event.getTitle()+" has been confirmed");
        notification.setActorSender(administratorService.findOne());
        notification.setMoment(new Date());
        messageRepository.save(notification);
    }

    public  void notifyRaffleWinner(Actor actor,Post post){
        Message notification;
        Collection<Actor> recievers;
        Collection<Actor> recievers2;

        Folder notificationBox;

        recievers = new ArrayList<>();
        recievers.add(actor);

        recievers2 = new ArrayList<>();
        recievers2.add(post.getActor());

        notificationBox = folderService.findActorAndFolder(actor.getId(),"notificationbox");

        notification = new Message();
        notification.setPriority("HIGH");
        notification.setActorReceivers(recievers);
        notification.setFolder(notificationBox);
        notification.setSubject("Congratulations");
        notification.setBody("Your are the winner of the raffle " +post.getTitle()+". Please contact with: " + post.getActor().getUserAccount().getUsername() +
            " to get your reward. \n Thanks for using Acme Style.");
        notification.setActorSender(administratorService.findOne());
        notification.setMoment(new Date());
        Message saved = messageRepository.save(notification);
        Message clone = saved.clone();
        clone.setActorReceivers(recievers2);
        clone.setFolder(folderService.findActorAndFolder(post.getActor().getId(),"notificationbox"));
        clone.setSubject("Raffle has finished");
        clone.setBody("The winner of the raffle " + post.getTitle() + " is " + actor.getUserAccount().getUsername() + " \n .Thanks for using Acme Style.");
        this.messageRepository.save(clone);
    }

    public void flush() {
        messageRepository.flush();
    }
}
