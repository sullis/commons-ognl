package org.apache.commons.ognl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This class has predefined instances that stand for OGNL's special "dynamic subscripts" for getting at the first,
 * middle, or last elements of a list. In OGNL expressions, these subscripts look like special kinds of array indexes:
 * [^] means the first element, [$] means the last, [|] means the middle, and [*] means the whole list.
 * 
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class DynamicSubscript
{
    public static final int FIRST = 0;

    public static final int MID = 1;

    public static final int LAST = 2;

    public static final int ALL = 3;

    public static final DynamicSubscript first = new DynamicSubscript( FIRST );

    public static final DynamicSubscript mid = new DynamicSubscript( MID );

    public static final DynamicSubscript last = new DynamicSubscript( LAST );

    public static final DynamicSubscript all = new DynamicSubscript( ALL );

    private int flag;

    private DynamicSubscript( int flag )
    {
        this.flag = flag;
    }

    public int getFlag()
    {
        return flag;
    }

    @Override
    public String toString()
    {
        switch ( flag )
        {
            case FIRST:
                return "^";
            case MID:
                return "|";
            case LAST:
                return "$";
            case ALL:
                return "*";
            default:
                return "?"; // Won't happen
        }
    }
}
