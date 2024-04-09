package at.ac.uibk.dps.cirrina.core.data;

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
                initial: true,
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
                  },
                  {
                    type: 'invoke',
                    serviceType: 'serviceTypeName',
                    isLocal: true,
                    input: [
                      {
                        name: 'inV1',
                        value: '5'
                      },
                      {
                        name: 'inV2',
                        value: '6'
                      }
                    ],
                    done: [
                      {
                        name: 'e1',
                        channel: 'internal'
                      },
                      {
                        name: 'e2',
                        channel: 'internal'
                      }
                    ]
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
            abstract: true,
            states: [
              {
                name: 'state1',
                virtual: true,
                initial: true,
                on: [
                  {
                    target: 'state2',
                    event: 'e1'
                  }
                ]
              },
              {
                name: 'state2',
                abstract: true
              },
              {
                name: 'state3',
                terminal: true
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
            ],
            guards: [
              {
                name: 'guard1',
                expression: 'true'
              },
              {
                name: 'guard2',
                expression: 'false'
              }
            ]
          },
          {
            name: 'stateMachine2',
            extends: 'stateMachine1',
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
                    target: 'state3',
                    event: 'e3'
                  }
                ]
              },
              {
                name: 'state4',
                 on: [
                  {
                    target: 'state3',
                    event: 'e4'
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
            ],
            guards: [
              {
                name: 'guard2',
                expression: 'true'
              }
            ]
          }
        ]
      }
      """;

  public static String invalidInheritance = """
      {
        name: 'collaborativeStateMachine',
        version: '0.1',
        memoryMode: 'distributed',
        stateMachines: [
          {
            name: 'stateMachine1',
            extends: 'invalidStateMachine',
            states: [
              {
                name: 'state1',
                initial: true,
                terminal: true
              }
            ]
          }
        ]
      }
      """;

  public static String invalidStateOverride = """
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
                initial: true,
                terminal: true
              }
            ]
          },
          {
            name: 'stateMachine2',
            extends: 'stateMachine1',
            states: [
              {
                name: 'state1'
              }
            ]
          }
        ]
      }
      """;

  public static String invalidAbstraction = """
      {
        name: 'collaborativeStateMachine',
        version: '0.1',
        memoryMode: 'distributed',
        stateMachines: [
          {
            name: 'stateMachine1',
            abstract: true,
            states: [
              {
                name: 'state1',
                initial: true,
                terminal: true
              },
              {
                name: 'state2',
                abstract: true
              }
            ]
          },
          {
            name: 'stateMachine2',
            extends: 'stateMachine1',
            states: [
              {
                name: 'state3'
              }
            ]
          }
        ]
      }
      """;

  public static String invalidAbstractStates = """
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
                initial: true,
                terminal: true
              },
              {
                name: 'state2',
                abstract: true
              }
            ]
          }
        ]
      }
      """;
}
