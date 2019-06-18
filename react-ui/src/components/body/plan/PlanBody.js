//react
import React from 'react';
//reactstrap
import { Button } from 'reactstrap';
//redux
import { connect } from 'react-redux';
//actions
import { plan } from "../../../actions/plan.js";
//css
import './planbody.css';

/**
 * Renders the plan button and the associated schema's plan when clicked
 *
 * @author Camilo Ortiz
 */

const PlanBody = ({selectedSchema, plan, getPlan}) => {
  return(
    <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
        boxShadow: "0 0 5px 2px #E0E0E0", width: "43rem"}}>

      <header className='body-name-plan'>
        Plan
      </header>

      {selectedSchema.selectedSchema != null ?
        <div>
          <div className='plan'>
            <Button color="secondary" block
                    style={{height: "4rem", width: "4rem", padding: "0, 2rem, 0, 1rem"}}
                    onClick={(e) => getPlan(selectedSchema.selectedSchema.id)}>
              Plan
            </Button>

            {plan.plan!==null?
              (plan.id === selectedSchema.selectedSchema.id ?
                <div style={{whiteSpace:"normal", width:"85%", height:"80%",
                              overflowX:"scroll", padding:"1rem, 1rem, 1rem, 2rem"}}>
                  {Object.keys(plan.plan)[0]}
                  <br/>
                  Cost: {Object.values(plan.plan)[0].cost}
                  <br/>
                  Value: {Object.values(plan.plan)[0].value}
                </div>
                :
                null
              )
            :
              null}
          </div>
        </div>

        :
        null
      }

    </div>
  );
}

//map states to props
const mapStatesToProps = (state) =>{
  return({
    selectedSchema: state.selectedSchema,
    plan: state.plan
  })
}

//map actions to props
const mapDispatchToProps = (dispatch) =>({
  getPlan: (id) => dispatch(plan(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(PlanBody);
