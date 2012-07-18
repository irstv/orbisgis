/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.core.renderer.se.parameter.string;

import java.util.*;
import javax.xml.bind.JAXBElement;
import net.opengis.se._2_0.core.ConcatenateType;
import net.opengis.se._2_0.core.ObjectFactory;
import net.opengis.se._2_0.core.ParameterValueType;
import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;

/**
 * Implementation of the {@code Concatenate} SE function. This function takes at
 * least to String in input, and simply concatenates them. it is consequently
 * only dependant on a list of {@code StringParameter} instances.</p>
 * <p>This class embedded a set of {@code StringParameter} instances, and can
 * be seen as a simplified list. It implements {@code Iterable} to ease the
 * processing of its content.
 * @author alexis
 */
public class StringConcatenate implements StringParameter, Iterable<StringParameter> {

        private List<StringParameter> inputStrings;

        /**
         * Build a new {@code StringConcatenate} instance from the given JAXB
         * {@code ConcatenateType} instance.
         * @param concatenate
         * @throws org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle
         */
        public StringConcatenate(ConcatenateType concatenate) throws InvalidStyle {
                List<ParameterValueType> jaxbList = concatenate.getStringValue();
                inputStrings = new ArrayList<StringParameter>(jaxbList.size());
                for(ParameterValueType pvt : jaxbList){
                        inputStrings.add(SeParameterFactory.createStringParameter(pvt));
                }
        }
        /**
         * Build a new {@code StringConcatenate} instance from the given 
         * {@code JAXBElement<ConcatenateType>} instance.
         * @param concatenate
         * @throws org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle
         */

        public StringConcatenate(JAXBElement<ConcatenateType> concat) throws InvalidStyle {
                this(concat.getValue());
        }

        @Override
        public String getValue(DataSource sds, long fid) throws ParameterException {
                List<String> inputs = new LinkedList<String>();
                int expectedSize = 0;
                for(StringParameter sp : inputStrings){
                        String tmp = sp.getValue(sds, fid);
                        inputs.add(tmp);
                        expectedSize+=tmp.length();
                }
                StringBuilder sb = new StringBuilder(expectedSize);
                for(String temps : inputs){
                        sb.append(temps);
                }
                return sb.toString();
        }

        @Override
        public String getValue(Map<String, Value> map) throws ParameterException {
                List<String> inputs = new LinkedList<String>();
                int expectedSize = 0;
                for(StringParameter sp : inputStrings){
                        String tmp = sp.getValue(map);
                        inputs.add(tmp);
                        expectedSize+=tmp.length();
                }
                StringBuilder sb = new StringBuilder(expectedSize);
                for(String temps : inputs){
                        sb.append(temps);
                }
                return sb.toString();
        }

        @Override
        public void setRestrictionTo(String[] list) {
        }

        @Override
        public HashSet<String> dependsOnFeature() {
                HashSet<String> sb = new HashSet<String>();
                for(StringParameter sp :inputStrings){
                        sb.addAll(sp.dependsOnFeature());
                }
                return sb;
        }

        @Override
        public ParameterValueType getJAXBParameterValueType() {
                ParameterValueType p = new ParameterValueType();
                p.getContent().add(this.getJAXBExpressionType());
                return p;
        }

        @Override
        public JAXBElement<?> getJAXBExpressionType() {
                ObjectFactory of = new ObjectFactory();
                ConcatenateType ct = new ConcatenateType();
                List<ParameterValueType> inc = ct.getStringValue();
                for(StringParameter sp : inputStrings){
                        inc.add(sp.getJAXBParameterValueType());
                }
                return of.createConcatenate(ct);
        }

        /**
         * Gets the number of StringParameter that are concatenated using this
         * function.
         * @return
         */
        public int size() {
                return inputStrings.size();
        }

        /**
         * Add a {@code StringParameter} to the input of this function.
         * @param e
         * @return
         */
        public boolean add(StringParameter e) {
                return inputStrings.add(e);
        }

        /**
         * Remove the first found obejct equals to {@code o} from the list of
         * inputs of this function.
         * @param o
         * @return {@code true} if some element has been removed.
         * @throws
         *      {@code ClassCastException} - if the type of the specified
         *      element is incompatible with this list
         */
        public boolean remove(Object o) {
                StringParameter sp = (StringParameter) o;
                return inputStrings.remove(sp);
        }

        /**
         * Reset the list of this function's inputs.
         */
        public void clear() {
                inputStrings.clear();
        }

        /**
         * Get the ith {@code StringParameter} to be concatenated.
         * @param index
         * @return
         */
        public StringParameter get(int index) {
                return inputStrings.get(index);
        }

        /**
         * Set the ith element to be concatenated to {@code element}.
         * @param index
         * @param element
         * @return
         *      the element that was previously at position {@code index}.
         * @throws
         *      {@code IndexOutOfBoundsException} - if the index is out of range
         *      {@code (index < 0 || index >= size()}).
         */
        public StringParameter set(int index, StringParameter element) {
                return inputStrings.set(index, element);
        }

        /**
         * Add (insert) {@code element} at the specified position.
         * @param index
         * @param element
         * @throws
         *      {@code IndexOutOfBoundsException} - if the index is out of range
         *      {@code (index < 0 || index > size()}).
         */
        public void add(int index, StringParameter element) {
                inputStrings.add(index, element);
        }

        /**
         * Remove the {@code StringParameter} registered at position {@code
         * index}.
         * @param index
         * @return
         * the removed StringParameter.
         * @throws
         *      {@code IndexOutOfBoundsException} - if the index is out of range
         *      {@code (index < 0 || index >= size()}).
         */
        public StringParameter remove(int index) {
                return inputStrings.remove(index);
        }

        /**
         * Gets a {@code ListIterator} representation of the underlying set of
         * {@code StringParameter} instances.
         * @return
         */
        public ListIterator<StringParameter> listIterator() {
                return inputStrings.listIterator();
        }

        @Override
        public Iterator<StringParameter> iterator() {
                return inputStrings.listIterator();
        }

}
