import { store } from '../reducers/store.js';

/**
 * Fetches plan object from the backend based on the provided selected schema's id.
 *
 * @author Camilo Ortiz
 */


export const fetchingPlan = () => {
    return {
        type: 'FETCHING_PLAN',
    };
}
export const resolvedPlan = (plan, schemaID, queryID) => {
  return{
    type: "RESOLVED_PLAN",
    schemaID,
    queryID,
    plan,
  }
}
export const errorPlan = () => {
    return {
        type: 'ERROR_PLAN',
    };
}



export const plan = (schemaID, queryID, SQL) => {
  //fetching
  store.dispatch(fetchingPlan());

  return function(dispatch, getState){
    let simpleSQL = SQL.replace(/\n|\r|\t/g, " ");

    return fetch("/plan/"+schemaID+"/"+queryID+"/"+simpleSQL)
    .then(res => res.text())
    .then(res => res = JSON.parse(res)).then((res)=>{
      if (res === null){
        dispatch(errorPlan())
      }
      dispatch(resolvedPlan(res, schemaID, queryID))
    }).catch(err => dispatch(errorPlan()));
  }
}