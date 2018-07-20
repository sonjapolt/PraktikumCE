# VirtualEnactment

A web-based application (using [Vaadin][1]), which implements the concept of "Elaboration through Virtual Enactment". This approach is used to reflect on, validate and modify existing work process models in collaborative settings of stakeholders. The details are summarized and scientifically grounded in a [working paper][2] I wrote. 

The motivation and larger context for this approach is grounded in the hypothesis that workers themselves should be supported in understanding how they are embedded in a collaborative socio-technical work system and should be able the models that aim at influencing their work processes. I have described this idea in much extent in an [article][3] that was published in the journal ["Information and Management"][4]. 

## Usage

The committed files contain the complete maven configuration for the project. It does not rely on any external libraries aside maven. Furthermore, the configuration files for the IntelliJ IDE are included - you might want to dismiss them, if you use something else.

## Branching Policy

From the commit tagged as release v2.0 on, the following branching policy applies:
- 'master' only contains release commits
- the main development branch is named 'develop'
- feature branches are branched and merged to 'develop' only
- release-branches are branched from 'develop' and are merged to 'master' only after testing
- merges are made as separate commits even if fast-forward merging would be possible to preserve the branch history

If you want to contribute, you might want to have a look at the open issues and projects specified in this repository. While [issues][5] point at concrete bugs or potential enhancements that have been identified during testing and real-world deployment, the [projects][6] outline the general directions of further development. Feel free to contact [me][7], if you have any questions on any of the projects, or simply comment on any of the open issues. 

## License

This software is provided under the GPL 3.0 license.

[1]:	http://www.vaadin.com
[2]:	https://zenodo.org/record/207008#.WFQGULGZOb8
[3]:	http://www.oppl.info/files/ArticulationOfWorkProcessModels.pdf
[4]:	http://www.journals.elsevier.com/information-and-management
[5]:	https://github.com/win-ce/VirtualEnactment/issues
[6]:	https://github.com/win-ce/VirtualEnactment/projects
[7]:	https://github.com/oppl