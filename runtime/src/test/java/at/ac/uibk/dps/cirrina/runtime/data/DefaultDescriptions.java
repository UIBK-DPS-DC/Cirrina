package at.ac.uibk.dps.cirrina.runtime.data;

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
                isInitial: true,
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
            isAbstract: true,
            states: [
              {
                name: 'state1',
                isVirtual: true,
                isInitial: true,
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
              },
              {
                name: 'state3',
                isTerminal: true
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
            ]
          }
        ]
      }
      """;

  public static String pingPong = """
        {
          name: 'collaborativeStateMachine',
          version: '0.1',
          memoryMode: 'distributed',
          localContext: {
            variables: [
              {
                name: 'v',
                value: '0'
              }
            ]
          },
          stateMachines: [
            {
              name: 'stateMachine1',
              states: [
                {
                  name: 'a',
                  isInitial: true,
                  entry: [
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
                      event: 'e2',
                      target: 'b'
                    }
                  ]
                },
                {
                  name: 'b',
                  entry: [
                    {
                      type: 'assign',
                      variable: {
                        name: 'v',
                        value: 'v + 1'
                      }
                    },
                    {
                      type: 'raise',
                      event: {
                        name: 'e3',
                        channel: 'internal'
                      }
                    }
                  ],
                  on: [
                    {
                      event: 'e4',
                      target: 'a'
                    }
                  ]
                }
              ]
            },
            {
              name: 'stateMachine2',
              states: [
                {
                  name: 'c',
                  isInitial: true,
                  entry: [
                    {
                      type: 'raise',
                      event: {
                        name: 'e4',
                        channel: 'internal'
                      }
                    }
                  ],
                  on: [
                    {
                      event: 'e1',
                      target: 'd'
                    }
                  ]
                },
                {
                  name: 'd',
                  entry: [
                    {
                      type: 'assign',
                      variable: {
                        name: 'v',
                        value: 'v + 1'
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
                      event: 'e3',
                      target: 'c'
                    }
                  ]
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
            inherit: 'invalidStateMachine',
            states: [
              {
                name: 'state1',
                isInitial: true,
                isTerminal: true
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
                isInitial: true,
                isTerminal: true
              }
            ]
          },
          {
            name: 'stateMachine2',
            inherit: 'stateMachine1',
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
            isAbstract: true,
            states: [
              {
                name: 'state1',
                isInitial: true,
                isTerminal: true
              },
              {
                name: 'state2',
                isAbstract: true
              }
            ]
          },
          {
            name: 'stateMachine2',
            inherit: 'stateMachine1',
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
                isInitial: true,
                isTerminal: true
              },
              {
                name: 'state2',
                isAbstract: true
              }
            ]
          }
        ]
      }
      """;
}
