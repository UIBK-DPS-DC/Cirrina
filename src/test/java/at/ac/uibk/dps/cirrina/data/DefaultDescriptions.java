package at.ac.uibk.dps.cirrina.data;

public class DefaultDescriptions {

  public static final String empty = "{}";

  public static final String complete = """
      {
        name: 'collaborativeStateMachine',
        version: '0.1',
        memoryMode: 'distributed',
        stateMachines: [
          {
            name: 'stateMachine1',
            states: [
              {
                name: 'state1',
                entry: [
                  {
                    reference: 'action1'
                  },
                  {
                    type: 'raise',
                    event: {
                      name: 'e1',
                      channel: 'internal'
                    }
                  }
                ],
                on: [
                  {
                    target: 'state2',
                    event: 'e1'
                  }
                ]
              },
              {
                name: 'state2',
                entry: [
                  {
                    type: 'assign',
                    variable: {
                      name: 'v',
                      value: 'v+1'
                    }
                  },
                  {
                    type: 'assign',
                    variable: {
                      name: 'v',
                      value: 'v+1'
                     }
                  },
                  {
                    type: 'raise',
                    event: {
                      name: 'e2',
                      channel: 'internal'
                    }
                  }
                ],
                on: [
                  {
                    target: 'state2',
                    event: 'e2'
                  }
                ]
              }
            ],
            actions: [
              {
                name: 'action1',
                type: 'create',
                variable: {
                  name: 'v',
                  value: '5'
                }
              }
            ]
          }
        ]
      }
      """;

  public static final String completeInheritance = """
      {
        name: 'collaborativeStateMachine',
        version: '0.1',
        memoryMode: 'distributed',
        stateMachines: [
          {
            name: 'stateMachine1',
            isAbstract: true,
            states: [
              {
                name: 'state1',
                isVirtual: true,
                on: [
                  {
                    target: 'state2',
                    event: 'e1'
                  }
                ]
              },
              {
                name: 'state2',
                isAbstract: true
              }
            ],
            actions: [
              {
                name: 'action1',
                type: 'assign',
                variable: {
                  name: 'v1',
                  value: '0'
                }
              },
              {
                name: 'action2',
                type: 'assign',
                variable: {
                  name: 'v2',
                  value: '1'
                }
              }
            ]
          },
          {
            name: 'stateMachine2',
            inherit: 'stateMachine1',
            states: [
              {
                name: 'state1',
                on: [
                  {
                    target: 'state2',
                    event: 'e2'
                  }
                ]
              },
              {
                name: 'state2',
                on: [
                  {
                    target: 'state2',
                    event: 'e3'
                  }
                ]
              }
            ],
            actions: [
              {
                name: 'action2',
                type: 'assign',
                variable: {
                  name: 'v2',
                  value: '2'
                }
              }
            ]
          }
        ]
      }
      """;
}
