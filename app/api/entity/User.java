package api.entity;

import api.exceptions.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.groupon.uuid.UUID;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.libs.Json;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "findUserByEmail", query = "SELECT e FROM User e WHERE e.email = :email"),
        @NamedQuery(name = "findUserByName", query = "SELECT e FROM User e WHERE e.name = :name")
})
public class User {
    @Id
    public String id;

    @Constraints.Email
    @Constraints.MaxLength(255)
    public String email;

    @Constraints.MaxLength(255)
    public String name;

    public String password;

    public String profilePic;

    public Boolean isAdmin;

    public Boolean isModerator;

    public User() {
        this.isAdmin = false;
        this.isModerator = false;
    }

    public User(User user) {
        this.id = user.id;
        this.email = user.email;
        this.name = user.name;
        this.password = user.password;
        this.profilePic = user.profilePic;
        this.isAdmin = user.isAdmin;
        this.isModerator = user.isModerator;
    }

    //////////// JPA methods ////////////////
    public String save() {
        this.id = new UUID().toString();
        if (StringUtils.isBlank(name)) {
            name = email;
        }
        JPA.em().persist(this);
        return this.id;
    }

    public static User getById(String id) {
        return JPA.em().find(User.class, id);
    }

    public static User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }

        try {
            //SQL injection will not happen because we are using setParameter
            return JPA.em().createNamedQuery("findUserByEmail", User.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static User getUserByName(String name) {
        if (name == null) {
            return null;
        }

        try {
            return JPA.em().createNamedQuery("findUserByName", User.class).setParameter("name", name).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static User getUserByNameOrEmail(String query) {
        if (query == null) {
            return null;
        }

        if (query.contains("@")) {
            return getUserByEmail(query);
        }

        return getUserByName(query);
    }
    //////////////////////////////////////////

    /////////// Methods required by Play form data binding /////////////////
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return null;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String validate() {
        if (name != null && name.contains("@")) {
            return "Cannot set user name to contain @.  It conflicts with emails";
        }

        if (email != null && !email.contains("@")) {
            return "Email must contain an @";
        }

        return null;
    }
    ////////////////////////////////////////////////////////////////////////

    public boolean checkPassword(String password) {
        if (password == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, this.password);
        } catch (Exception e) {
            Logger.warn(e.getMessage());
            return false;
        }
    }

    public JsonNode asJSONObject() {
        return Json.newObject()
                .put("name", name)
                .put("email", email)
                .put("profilePic", profilePic);
    }

    public boolean patch(User patch) throws ValidationException {
        boolean changed = false;
        if (patch.name != null && !patch.name.equals(this.name)) {
            if (getUserByName(patch.name) == null) {
                //no need to check for @ symbol because validate function should take care of that
                this.name = patch.name;
                changed = true;
            } else {
                throw new ValidationException("Name is already taken");
            }

        }

        if (patch.email != null && !patch.email.equals(this.email)) {
            if (getUserByEmail(patch.email) == null) {
                this.email = patch.email;
                if (this.name.contains("@")) {
                    this.name = this.email;
                }
                changed = true;
            } else {
                throw new ValidationException("Email is already taken");
            }
        }

        if (patch.password != null) {
            this.password = patch.password;
            changed = true;
        }

        if (patch.profilePic != null && !patch.profilePic.equals(this.profilePic)) {
            S3File file = S3File.getByPublicId(patch.profilePic);
            if (file != null && file.getOwnerId().equals(this.id)) {
                this.profilePic = patch.profilePic;
                changed = true;
            } else {
                throw new ValidationException("Invalid profilePic id");
            }
        }

        if (changed) {
            JPA.em().persist(this);
        }

        return changed;
    }
}
