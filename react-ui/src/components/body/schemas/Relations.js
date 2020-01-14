import React from 'react';
import { FaListUl } from 'react-icons/fa';
import PopoutWindow from '../Popout';
import Button from 'react-bootstrap/Button';
import { IconContext } from "react-icons";
import ListGroup from 'react-bootstrap/ListGroup';
import { Table,
         Modal,
         ModalHeader,
         ModalBody,
         ModalFooter,
         Tooltip,
} from 'reactstrap';


export default class Relations extends React.Component{
  constructor(props){
    super(props);
    this.toggleRelationsModal = this.toggleRelationsModal.bind(this);
    this.toggleTooltip = this.toggleTooltip.bind(this);
      this.state = {
        modalRelationsOpen: false,
        tooltipOpen: false
      };
  }

  toggleRelationsModal(){
    this.setState({
      modalRelationsOpen: !this.state.modalRelationsOpen,
      tooltipOpen: false
    });
  }

  toggleTooltip(){
    this.setState({
      tooltipOpen: !this.state.tooltipOpen
    })
  }

  openRelations(id){
    this.props.getRelations(id);

    this.toggleRelationsModal();
  }

  render(){
    return(
      <div>
        <Button
          id={"RelationButton"+ this.props.schemaFromList.id}
          variant='link'
          onClick={()=> this.openRelations(this.props.schemaFromList.id)}
        >
          <IconContext.Provider value={{ style:{ margin: '0', padding: '0'} }}>
            <FaListUl/>
          </IconContext.Provider>
        </Button>

        <Tooltip
          trigger="hover"
          placement="top"
          isOpen={this.state.tooltipOpen && !this.state.modalRelationsOpen}
          target={"RelationButton"+ this.props.schemaFromList.id}
          toggle={this.toggleTooltip}>
          View Relations
        </Tooltip>

        <Modal
          size="lg"
          isOpen={this.state.modalRelationsOpen}
          toggle={this.toggleRelationsModal}>
          <ModalHeader toggle={this.toggleRelationsModal}>
            Relations
          </ModalHeader>
          <ModalBody>

          { this.props.relationList.relationList.relations != null ?
            <ListGroup variant="flush">
            {this.props.relationList.relationList.relations.map((relation, index)=>{
              return(
                <NestedRelations key={"relation"+index} relation={relation}/>
              )
            })}
          </ListGroup>
          :
          null
          }

          </ModalBody>

          <ModalFooter>
            <Button
              variant="secondary"
              onClick={this.toggleRelationsModal}>Cancel</Button>
          </ModalFooter>
        </Modal>
      </div>
    )
  }
}

//nested modal class that displays each relation's information
 class NestedRelations extends React.Component {
   constructor(props) {
     super(props);
     this.state = {
       modal: false
     };

     this.toggle = this.toggle.bind(this);
   }

   toggle() {
     this.setState(prevState => ({
       modal: !prevState.modal
     }));
   }

   render() {
     const popoutContent = (relation) => (
       <div>
          <header>{relation.name}</header>
         <RelationAttributeTable relation={relation}/>
         <RelationAccessTable relation={relation}/>
       </div>
     );
     return (
       <div>
         <ListGroup.Item
            action
            onClick={this.toggle}
          >
              {this.props.relation.name}
          </ListGroup.Item>

         <Modal
          size='lg'
          isOpen={this.state.modal}
          toggle={this.toggle}>

           <ModalHeader toggle={this.toggle}>
             {this.props.relation.name}
             <PopoutWindow
              content={popoutContent(this.props.relation)}
              title={"Relations"}
              />
           </ModalHeader>

           <ModalBody>
              <RelationAttributeTable relation={this.props.relation}/>
              <RelationAccessTable relation={this.props.relation}/>
           </ModalBody>

           <ModalFooter>
             <Button
                variant="secondary"
                onClick={this.toggle}>Cancel</Button>
           </ModalFooter>
         </Modal>
       </div>
     );
   }
 }

//table for displaying each relation's attributes
const RelationAttributeTable = ({relation}) => {
  return(
    <div>
      <h5>Attributes</h5>
      <Table>
        <thead>
          <tr>
            <th>#</th>
            <th>Name</th>
            <th>Type</th>
          </tr>
        </thead>

        <tbody>
          {relation.attributes.map((attribute, index) => {
            return[
              <tr key={"row"+index}>
                <th scope="row">{index+1}</th>
                <td>{attribute.name}</td>
                <td>{attribute.type}</td>
              </tr>
            ]
          })}

        </tbody>

      </Table>
    </div>
  )
}

//table for displaying each relation's access methods
const RelationAccessTable = ({relation}) => {
  return(
    <div>
    {relation.accessMethods.length > 0 ?
      <div>
        <h5>Access Methods</h5>
        <Table>
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              <th>Type</th>
            </tr>
          </thead>

          <tbody>
            {relation.accessMethods.map((method, index) => {
              return[
                <tr key={"row"+index}>
                  <th scope="row">{index+1}</th>
                  <td>{method.name}</td>
                  <td>{method.type}</td>
                </tr>
              ]
            })}

          </tbody>

        </Table>
      </div>
      :
      <div>This relation has no access methods.</div>
    }
    </div>
  )
}
