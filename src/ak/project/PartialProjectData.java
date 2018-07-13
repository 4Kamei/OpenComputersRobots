package ak.project;

import ak.project.Project;
import ak.project.ProjectType;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Aleksander on 21:54, 11/07/2018.
 */
public class PartialProjectData {
    private ProjectType type;
    @SerializedName("data")
    private String projectData;

    public ProjectType getType() {
        return type;
    }

    public Project getProject(int id) {
        switch (type) {

            case MINE_PROJECT: {

                MineProjectData d = new Gson().fromJson(projectData, MineProjectData.class);
                System.out.println(projectData);

                return new MineProject(id,
                        d.getX(),
                        d.getY(),
                        d.getZ(),
                        d.getSizeX(),
                        d.getSizeY(),
                        d.getSizeZ()
                );

            }
            default: return null;
        }
    }
}
